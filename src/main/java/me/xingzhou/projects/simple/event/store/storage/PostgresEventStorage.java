package me.xingzhou.projects.simple.event.store.storage;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;
import static me.xingzhou.projects.simple.event.store.storage.PostgresEventStorage.ErrorCodes.UNIQUE_VIOLATION;
import static me.xingzhou.projects.simple.event.store.storage.PostgresEventStorage.Queries.INSERT_EVENT_QUERY;
import static me.xingzhou.projects.simple.event.store.storage.PostgresEventStorage.Queries.LATEST_STREAM_EVENT_QUERY;
import static me.xingzhou.projects.simple.event.store.storage.PostgresEventStorage.Schema.Columns.*;
import static me.xingzhou.projects.simple.event.store.storage.PostgresEventStorage.Schema.Tables.EVENTS_TABLE;
import static me.xingzhou.projects.simple.event.store.storage.PostgresEventStorage.Schema.Tables.LATEST_STREAM_EVENT_TABLE;

import jakarta.annotation.Nonnull;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.NoSuchStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.StaleVersionFailure;

public class PostgresEventStorage implements EventStorage {
    private final DataSource dataSource;

    public PostgresEventStorage(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Nonnull
    @Override
    public StoredRecord appendEvent(
            @Nonnull String streamName, long currentVersion, @Nonnull String eventType, @Nonnull String eventContent) {
        if (isCreateStreamRequest(currentVersion)) {
            return createStream(streamName, eventType, eventContent);
        }
        return appendToStream(streamName, currentVersion, eventType, eventContent);
    }

    @Nonnull
    @Override
    public VersionedRecords retrieveEvents(
            @Nonnull String streamName,
            @Nonnull List<String> eventTypes,
            long exclusiveStartVersion,
            long inclusiveEndVersion) {
        if (streamExists(streamName)) {
            throw new UnsupportedOperationException("Not Implemented");
        }
        throw new NoSuchStreamFailure(streamName);
    }

    @Nonnull
    @Override
    public TimestampedRecords retrieveEvents(
            long exclusiveStartId,
            long inclusiveEndId,
            @Nonnull List<String> streamNames,
            @Nonnull List<String> eventTypes) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    private StoredRecord appendToStream(String streamName, long currentVersion, String eventType, String eventContent) {
        return handleExceptions(() -> {
            if (streamExists(streamName)) {
                try {
                    return insertRecord(streamName, eventType, eventContent, currentVersion + 1);
                } catch (SQLException e) {
                    if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                        throw new StaleVersionFailure();
                    }
                    throw e;
                }
            }
            throw new NoSuchStreamFailure(streamName);
        });
    }

    private StoredRecord createStream(String streamName, String eventType, String eventContent) {
        return handleExceptions(() -> {
            try {
                return insertRecord(streamName, eventType, eventContent, Constants.Versions.NEW_STREAM);
            } catch (SQLException e) {
                if (UNIQUE_VIOLATION.equals(e.getSQLState())) {
                    throw new DuplicateEventStreamFailure();
                }
                throw e;
            }
        });
    }

    private StoredRecord insertRecord(String streamName, String eventType, String eventContent, long version)
            throws SQLException {
        try (var connection = dataSource.getConnection();
                var statement = connection.prepareStatement(INSERT_EVENT_QUERY)) {
            statement.setString(1, streamName);
            statement.setString(2, eventType);
            statement.setString(3, eventContent);
            statement.setLong(4, version);
            var resultSet = statement.executeQuery();
            resultSet.next();
            var id = resultSet.getLong(ID);
            var insertedOn = resultSet.getTimestamp(INSERTED_ON).toInstant();
            return new StoredRecord(id, streamName, eventType, eventContent, version, insertedOn);
        }
    }

    private boolean isCreateStreamRequest(long currentVersion) {
        return currentVersion == Constants.Versions.UNDEFINED_STREAM;
    }

    private boolean streamExists(String streamName) {
        return handleExceptions(() -> {
            try (var connection = dataSource.getConnection();
                    var statement = connection.prepareStatement(LATEST_STREAM_EVENT_QUERY)) {
                statement.setString(1, streamName);
                var resultSet = statement.executeQuery();
                return resultSet.next();
            }
        });
    }

    interface ErrorCodes {
        String UNIQUE_VIOLATION = "23505";
    }

    interface Queries {
        String INSERT_EVENT_QUERY =
                // spotless:off
                " INSERT INTO " + EVENTS_TABLE + " (" + STREAM_NAME + ", " + EVENT_TYPE + ", " + EVENT_CONTENT + ", " + VERSION + ")" +
                " VALUES (?, ?, ?::jsonb, ?)" +
                " RETURNING " + ID + ", " + INSERTED_ON;
                //spotless:on

        String LATEST_STREAM_EVENT_QUERY =
                // spotless:off
                " SELECT * FROM " + LATEST_STREAM_EVENT_TABLE +
                " WHERE " + STREAM_NAME + " = ?";
                //spotless:on
    }

    interface Schema {
        interface Columns {
            String ID = "id";
            String STREAM_NAME = "stream_name";
            String EVENT_TYPE = "event_type";
            String EVENT_CONTENT = "event_content";
            String VERSION = "version";
            String INSERTED_ON = "inserted_on";
        }

        interface Tables {
            String EVENTS_TABLE = "eventsource.events";
            String LATEST_STREAM_EVENT_TABLE = "eventsource.latest_stream_event";
        }
    }
}
