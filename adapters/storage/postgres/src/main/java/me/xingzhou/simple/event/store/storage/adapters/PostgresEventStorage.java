package me.xingzhou.simple.event.store.storage.adapters;

import static me.xingzhou.simple.event.store.storage.adapters.PostgresEventStorage.ErrorCodes.UNIQUE_VIOLATION;
import static me.xingzhou.simple.event.store.storage.adapters.PostgresEventStorage.Queries.*;
import static me.xingzhou.simple.event.store.storage.adapters.PostgresEventStorage.Schema.Columns.*;
import static me.xingzhou.simple.event.store.storage.adapters.PostgresEventStorage.Schema.Tables.*;
import static me.xingzhou.simple.event.store.storage.adapters.internal.CheckedExceptionHandlers.handleExceptions;

import jakarta.annotation.Nonnull;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.sql.DataSource;
import me.xingzhou.simple.event.store.storage.EventStorage;
import me.xingzhou.simple.event.store.storage.StoredRecord;
import me.xingzhou.simple.event.store.storage.TimestampedRecords;
import me.xingzhou.simple.event.store.storage.VersionedRecords;
import me.xingzhou.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.simple.event.store.storage.failures.NoSuchStreamFailure;
import me.xingzhou.simple.event.store.storage.failures.StaleVersionFailure;

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
        return handleExceptions(() -> {
            try (var connection = dataSource.getConnection()) {
                var latestRecord = getLatestRecord(connection, this::extractRecord);
                var records = getRecords(
                        exclusiveStartId, inclusiveEndId, streamNames, eventTypes, connection, this::extractRecords);
                return new TimestampedRecords(records, latestRecord);
            }
        });
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

    private StoredRecord extractRecord(ResultSet resultSet) {
        return handleExceptions(() -> new StoredRecord(
                resultSet.getLong(ID),
                resultSet.getString(STREAM_NAME),
                resultSet.getString(EVENT_TYPE),
                resultSet.getString(EVENT_CONTENT),
                resultSet.getLong(VERSION),
                resultSet.getTimestamp(INSERTED_ON).toInstant()));
    }

    private List<StoredRecord> extractRecords(ResultSet resultSet) {
        return handleExceptions(() -> {
            var list = new ArrayList<StoredRecord>();
            while (resultSet.next()) {
                var record = extractRecord(resultSet);
                list.add(record);
            }
            return list;
        });
    }

    private StoredRecord getLatestRecord(Connection connection, Function<ResultSet, StoredRecord> extractRecord)
            throws SQLException {
        try (var statement = connection.prepareStatement(LATEST_EVENT_QUERY)) {
            var resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return extractRecord.apply(resultSet);
            }
            return StoredRecord.emptyRecord();
        }
    }

    private StoredRecord getLatestRecord(Connection connection, String streamName) throws SQLException {
        try (var statement = connection.prepareStatement(LATEST_STREAM_EVENT_QUERY)) {
            statement.setString(1, streamName);
            var resultSet = statement.executeQuery();
            resultSet.next();
            return extractRecord(resultSet);
        }
    }

    private List<StoredRecord> getRecords(
            String streamName,
            List<String> eventTypes,
            long exclusiveStartVersion,
            long inclusiveEndVersion,
            Connection connection)
            throws SQLException {
        if (eventTypes.isEmpty()) {
            try (var retrieveEventsStatement = connection.prepareStatement(RETRIEVE_STREAM_EVENTS)) {
                retrieveEventsStatement.setString(1, streamName);
                retrieveEventsStatement.setLong(2, exclusiveStartVersion);
                retrieveEventsStatement.setLong(3, inclusiveEndVersion);
                var resultSet = retrieveEventsStatement.executeQuery();
                return extractRecords(resultSet);
            }
        }

        try (var retrieveEventsStatement = connection.prepareStatement(RETRIEVE_STREAM_EVENTS_BY_TYPES)) {
            retrieveEventsStatement.setString(1, streamName);
            retrieveEventsStatement.setLong(2, exclusiveStartVersion);
            retrieveEventsStatement.setLong(3, inclusiveEndVersion);
            retrieveEventsStatement.setArray(4, connection.createArrayOf("text", eventTypes.toArray()));
            var resultSet = retrieveEventsStatement.executeQuery();
            return extractRecords(resultSet);
        }
    }

    private List<StoredRecord> getRecords(
            long exclusiveStartId,
            long inclusiveEndId,
            List<String> streamNames,
            List<String> eventTypes,
            Connection connection,
            Function<ResultSet, List<StoredRecord>> extractRecords)
            throws SQLException {
        if (eventTypes.isEmpty() && streamNames.isEmpty()) {
            try (var retrieveEventsStatement = connection.prepareStatement(RETRIEVE_EVENTS)) {
                retrieveEventsStatement.setLong(1, exclusiveStartId);
                retrieveEventsStatement.setLong(2, inclusiveEndId);
                var resultSet = retrieveEventsStatement.executeQuery();
                return extractRecords(resultSet);
            }
        }

        if (eventTypes.isEmpty()) {
            try (var retrieveEventsStatement = connection.prepareStatement(RETRIEVE_EVENTS_IN_STREAMS)) {
                retrieveEventsStatement.setLong(1, exclusiveStartId);
                retrieveEventsStatement.setLong(2, inclusiveEndId);
                retrieveEventsStatement.setArray(3, connection.createArrayOf("text", streamNames.toArray()));
                var resultSet = retrieveEventsStatement.executeQuery();
                return extractRecords(resultSet);
            }
        }

        if (streamNames.isEmpty()) {
            try (var retrieveEventsStatement = connection.prepareStatement(RETRIEVE_EVENTS_BY_TYPES)) {
                retrieveEventsStatement.setLong(1, exclusiveStartId);
                retrieveEventsStatement.setLong(2, inclusiveEndId);
                retrieveEventsStatement.setArray(3, connection.createArrayOf("text", eventTypes.toArray()));
                var resultSet = retrieveEventsStatement.executeQuery();
                return extractRecords(resultSet);
            }
        }

        try (var retrieveEventsStatement = connection.prepareStatement(RETRIEVE_EVENTS_IN_STREAMS_AND_BY_TYPES)) {
            retrieveEventsStatement.setLong(1, exclusiveStartId);
            retrieveEventsStatement.setLong(2, inclusiveEndId);
            retrieveEventsStatement.setArray(3, connection.createArrayOf("text", streamNames.toArray()));
            retrieveEventsStatement.setArray(4, connection.createArrayOf("text", eventTypes.toArray()));
            var resultSet = retrieveEventsStatement.executeQuery();
            return extractRecords(resultSet);
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
                var latestRecord = getLatestRecord(connection, streamName);
                var records =
                        getRecords(streamName, eventTypes, exclusiveStartVersion, inclusiveEndVersion, connection);
                return new VersionedRecords(records, latestRecord);
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

        String LATEST_EVENT_QUERY =
                // spotless:off
                " SELECT * FROM " + LATEST_EVENT_TABLE;
                //spotless:on

        String LATEST_STREAM_EVENT_QUERY =
                // spotless:off
                " SELECT * FROM " + LATEST_STREAM_EVENT_TABLE +
                " WHERE " + STREAM_NAME + " = ?";
                //spotless:on

        String RETRIEVE_STREAM_EVENTS_BY_TYPES =
                // spotless:off
                " SELECT * FROM " + EVENTS_TABLE +
                " WHERE " + STREAM_NAME + " = ?" +
                " AND " + VERSION + " > ? AND " + VERSION + " <= ?" +
                " AND " + EVENT_TYPE + " = ANY(?::text[])" +
                " ORDER BY " + VERSION;
                //spotless:on

        String RETRIEVE_STREAM_EVENTS =
                // spotless:off
                " SELECT * FROM " + EVENTS_TABLE +
                " WHERE " + STREAM_NAME + " = ?" +
                " AND " + VERSION + " > ? AND " + VERSION + " <= ?" +
                " ORDER BY " + VERSION;
                //spotless:on

        String RETRIEVE_EVENTS =
                // spotless:off
                " SELECT * FROM " + EVENTS_TABLE +
                " WHERE " + ID + " > ? AND " + ID + " <= ?" +
                " ORDER BY " + ID;
                //spotless:on

        String RETRIEVE_EVENTS_IN_STREAMS =
                // spotless:off
                " SELECT * FROM " + EVENTS_TABLE +
                " WHERE " + ID + " > ? AND " + ID + " <= ?" +
                " AND " + STREAM_NAME + " = ANY(?::text[])" +
                " ORDER BY " + ID;
                //spotless:on

        String RETRIEVE_EVENTS_BY_TYPES =
                // spotless:off
                " SELECT * FROM " + EVENTS_TABLE +
                " WHERE " + ID + " > ? AND " + ID + " <= ?" +
                " AND " + EVENT_TYPE + " = ANY(?::text[])" +
                " ORDER BY " + ID;
                //spotless:on

        String RETRIEVE_EVENTS_IN_STREAMS_AND_BY_TYPES =
                // spotless:off
                " SELECT * FROM " + EVENTS_TABLE +
                " WHERE " + ID + " > ? AND " + ID + " <= ?" +
                " AND " + STREAM_NAME + " = ANY(?::text[])" +
                " AND " + EVENT_TYPE + " = ANY(?::text[])" +
                " ORDER BY " + ID;
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
            String LATEST_EVENT_TABLE = "eventsource.latest_event";
        }
    }
}
