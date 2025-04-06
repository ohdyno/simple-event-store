package me.xingzhou.projects.simple.event.store.storage;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.*;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.NoSuchStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.StaleVersionFailure;

public class InMemoryEventStorage implements EventStorage {
    private final List<StoredRecord> storage = new ArrayList<>();

    private final Set<String> streamNamesIndex = new HashSet<>();

    private Instant lastUpdateAt = TimestampConstants.NEVER;

    @Override
    public StoredRecord appendEvent(
            @Nonnull String streamName, long currentVersion, @Nonnull String eventType, @Nonnull String eventContent) {
        if (isCreateStreamRequest(currentVersion)) {
            return createStream(streamName, eventType, eventContent);
        }
        return appendToStream(streamName, currentVersion, eventType, eventContent);
    }

    @Override
    public @Nonnull VersionedRecords retrieveEvents(
            @Nonnull String streamName,
            @Nonnull List<String> eventTypes,
            long exclusiveStartVersion,
            long inclusiveEndVersion) {
        var eventStream = getEventStream(streamName);
        var version = getCurrentVersion(streamName);
        var records = eventStream.stream()
                .filter(event -> shouldIncludeEvent(event, eventTypes, exclusiveStartVersion, inclusiveEndVersion))
                .toList();
        return new VersionedRecords(records, version);
    }

    @Override
    public @Nonnull TimestampedRecords retrieveEvents(
            long exclusiveStart,
            long inclusiveEnd,
            @Nonnull List<String> streamNames,
            @Nonnull List<String> eventTypes) {
        var records = storage.stream()
                .filter(event -> shouldIncludeEvent(event, exclusiveStart, inclusiveEnd, streamNames, eventTypes))
                .toList();
        return new TimestampedRecords(records, lastUpdateAt);
    }

    private StoredRecord appendToStream(String streamName, long currentVersion, String eventType, String eventContent) {
        if (getCurrentVersion(streamName) == currentVersion) {
            var record = new StoredRecord(
                    createEventId(), streamName, eventType, eventContent, currentVersion + 1, Instant.now());
            save(streamName, record);
            return record;
        }
        throw new StaleVersionFailure();
    }

    private long createEventId() {
        return storage.size() + Constants.Ids.START;
    }

    private StoredRecord createStream(String streamName, String eventType, String eventContent) {
        if (streamNamesIndex.contains(streamName)) {
            throw new DuplicateEventStreamFailure();
        }
        var record = new StoredRecord(
                createEventId(), streamName, eventType, eventContent, VersionConstants.NEW_STREAM, Instant.now());
        save(streamName, record);
        return record;
    }

    /**
     * @param streamName is the name of a stream within {@link #storage}.
     * @throws NoSuchStreamFailure if the stream does not exist.
     * @return the current version for the stream, accounting for the value of {@link VersionConstants#NEW_STREAM}
     */
    private int getCurrentVersion(String streamName) {
        return getEventStream(streamName).size() - 1;
    }

    /**
     * @param streamName is the name of a stream within {@link #storage}.
     * @return the records for the stream in insertion order.
     * @throws NoSuchStreamFailure if the stream does not exist.
     */
    private List<StoredRecord> getEventStream(String streamName) {
        if (streamNamesIndex.contains(streamName)) {
            return storage.stream()
                    .filter(record -> record.streamName().equals(streamName))
                    .toList();
        }
        throw new NoSuchStreamFailure(streamName);
    }

    private boolean isCreateStreamRequest(long currentVersion) {
        return currentVersion == VersionConstants.UNDEFINED_STREAM;
    }

    private void save(String streamName, StoredRecord record) {
        storage.add(record);
        streamNamesIndex.add(streamName);
        lastUpdateAt = record.timestamp();
    }

    private boolean shouldIncludeEvent(
            StoredRecord event, List<String> eventTypes, long exclusiveStartVersion, long inclusiveEndVersion) {
        var isCorrectType = eventTypes.isEmpty() || eventTypes.contains(event.eventType());
        var isWithinRange = exclusiveStartVersion < event.version() && event.version() <= inclusiveEndVersion;
        return isCorrectType && isWithinRange;
    }

    private boolean shouldIncludeEvent(
            StoredRecord event,
            long exclusiveStart,
            long exclusiveEnd,
            List<String> streamNames,
            List<String> eventTypes) {
        var isInStreams = streamNames.isEmpty() || streamNames.contains(event.streamName());
        var isCorrectType = eventTypes.isEmpty() || eventTypes.contains(event.eventType());
        return isInStreams && isCorrectType;
    }
}
