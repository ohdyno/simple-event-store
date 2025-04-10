package me.xingzhou.projects.simple.event.store.storage;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;
import static me.xingzhou.projects.simple.event.store.storage.PostgresEventStorage.ErrorCodes.UNIQUE_VIOLATION;
import static me.xingzhou.projects.simple.event.store.storage.PostgresEventStorage.Queries.*;
import static me.xingzhou.projects.simple.event.store.storage.PostgresEventStorage.Schema.Columns.*;
import static me.xingzhou.projects.simple.event.store.storage.PostgresEventStorage.Schema.Tables.EVENTS_TABLE;
import static me.xingzhou.projects.simple.event.store.storage.PostgresEventStorage.Schema.Tables.LATEST_STREAM_EVENT_TABLE;

import jakarta.annotation.Nonnull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
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
            return retrieveStreamEvents(streamName, eventTypes, exclusiveStartVersion, inclusiveEndVersion);
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

    private List<StoredRecord> extractRecords(ResultSet resultSet) {
        return handleExceptions(() -> {
            var list = new ArrayList<StoredRecord>();
            while (resultSet.next()) {
                var record = new StoredRecord(
                        resultSet.getLong(ID),
                        resultSet.getString(STREAM_NAME),
                        resultSet.getString(EVENT_TYPE),
                        resultSet.getString(EVENT_CONTENT),
                        resultSet.getLong(VERSION),
                        resultSet.getTimestamp(INSERTED_ON).toInstant());
                list.add(record);
            }
            return list;
        });
    }

    private long getLatestVersion(Connection connection, String streamName) throws SQLException {
        try (var statement = connection.prepareStatement(LATEST_STREAM_EVENT_QUERY)) {
            statement.setString(1, streamName);
            var resultSet = statement.executeQuery();
            resultSet.next();
            return resultSet.getLong(VERSION);
        }
    }

    private List<StoredRecord> getResults(
            String streamName,
            List<String> eventTypes,
            long exclusiveStartVersion,
            long inclusiveEndVersion,
            Connection connection,
            Function<ResultSet, List<StoredRecord>> extractRecord)
            throws SQLException {
        if (eventTypes.isEmpty()) {
            try (var retrieveEventsStatement = connection.prepareStatement(Queries.RETRIEVE_STREAM_EVENTS_ALL_EVENTS)) {
                retrieveEventsStatement.setString(1, streamName);
                retrieveEventsStatement.setLong(2, exclusiveStartVersion);
                retrieveEventsStatement.setLong(3, inclusiveEndVersion);
                var resultSet = retrieveEventsStatement.executeQuery();
                return extractRecord.apply(resultSet);
            }
        }

        try (var retrieveEventsStatement = connection.prepareStatement(RETRIEVE_STREAM_EVENTS)) {
            retrieveEventsStatement.setString(1, streamName);
            retrieveEventsStatement.setLong(2, exclusiveStartVersion);
            retrieveEventsStatement.setLong(3, inclusiveEndVersion);
            retrieveEventsStatement.setArray(4, connection.createArrayOf("text", eventTypes.toArray()));
            var resultSet = retrieveEventsStatement.executeQuery();
            return extractRecord.apply(resultSet);
        }
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

    private VersionedRecords retrieveStreamEvents(
            String streamName, List<String> eventTypes, long exclusiveStartVersion, long inclusiveEndVersion) {
        return handleExceptions(() -> {
            try (var connection = dataSource.getConnection()) {
                var latestVersion = getLatestVersion(connection, streamName);
                var records = getResults(
                        streamName,
                        eventTypes,
                        exclusiveStartVersion,
                        inclusiveEndVersion,
                        connection,
                        this::extractRecords);
                return new VersionedRecords(records, latestVersion);
            }
        });
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

        String RETRIEVE_STREAM_EVENTS =
                // spotless:off
                " SELECT * FROM " + EVENTS_TABLE +
                " WHERE " + STREAM_NAME + " = ?" +
                " AND " + VERSION + " > ? AND " + VERSION + " <= ?" +
                " AND " + EVENT_TYPE + " = ANY(?::text[])";
                //spotless:on

        String RETRIEVE_STREAM_EVENTS_ALL_EVENTS =
                // spotless:off
                " SELECT * FROM " + EVENTS_TABLE +
                " WHERE " + STREAM_NAME + " = ?" +
                " AND " + VERSION + " > ? AND " + VERSION + " <= ?";
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
