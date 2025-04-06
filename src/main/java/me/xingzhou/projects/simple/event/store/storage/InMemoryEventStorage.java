package me.xingzhou.projects.simple.event.store.storage;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.NoSuchStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.StaleVersionFailure;

public class InMemoryEventStorage implements EventStorage {
    private final List<EventRecord> storage = new ArrayList<>();
    private final Set<String> streamNamesIndex = new HashSet<>();
    private Instant lastUpdateAt = TimestampConstants.NEVER;

    @Override
    public long createStream(
            @Nonnull String streamName,
            @Nonnull String eventId,
            @Nonnull String eventType,
            @Nonnull String eventContent) {
        if (streamNamesIndex.contains(streamName)) {
            throw new DuplicateEventStreamFailure();
        }
        var record = new EventRecord(
                streamName, eventId, eventType, eventContent, VersionConstants.NEW_STREAM, Instant.now());
        save(streamName, record);
        return record.version();
    }

    @Override
    public long appendEvent(
            @Nonnull String streamName,
            long currentVersion,
            @Nonnull String eventId,
            @Nonnull String eventType,
            @Nonnull String eventContent) {
        if (getCurrentVersion(streamName) == currentVersion) {
            var record =
                    new EventRecord(streamName, eventId, eventType, eventContent, currentVersion + 1, Instant.now());
            save(streamName, record);
            return record.version();
        }
        throw new StaleVersionFailure();
    }

    /**
     * @param streamName is the name of a stream within {@link #storage}.
     * @throws NoSuchStreamFailure if the stream does not exist.
     * @return the current version for the stream, accounting for the value of {@link VersionConstants#NEW_STREAM}
     */
    private int getCurrentVersion(String streamName) {
        return getEventStream(streamName).size() - 1;
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
                .map(EventRecord::toStoredRecord)
                .toList();
        return new VersionedRecords(records, version);
    }

    @Override
    public @Nonnull TimestampedRecords retrieveEvents(
            @Nonnull Instant exclusiveStart,
            @Nonnull Instant exclusiveEnd,
            @Nonnull List<String> streamNames,
            @Nonnull List<String> eventTypes) {
        var records = storage.stream()
                .filter(event -> shouldIncludeEvent(event, exclusiveStart, exclusiveEnd, streamNames, eventTypes))
                .map(EventRecord::toStoredRecord)
                .toList();
        return new TimestampedRecords(records, lastUpdateAt());
    }

    private boolean shouldIncludeEvent(
            EventRecord event,
            Instant exclusiveStart,
            Instant exclusiveEnd,
            List<String> streamNames,
            List<String> eventTypes) {
        var isInStreams = streamNames.isEmpty() || streamNames.contains(event.streamName());
        var isCorrectType = eventTypes.isEmpty() || eventTypes.contains(event.eventType());
        return isInStreams && isCorrectType;
    }

    @Override
    public Instant lastUpdateAt() {
        return lastUpdateAt;
    }

    private static boolean shouldIncludeEvent(
            EventRecord event, List<String> eventTypes, long exclusiveStartVersion, long inclusiveEndVersion) {
        var isCorrectType = eventTypes.isEmpty() || eventTypes.contains(event.eventType());
        var isWithinRange = exclusiveStartVersion < event.version() && event.version() <= inclusiveEndVersion;
        return isCorrectType && isWithinRange;
    }

    /**
     * @param streamName is the name of a stream within {@link #storage}.
     * @throws NoSuchStreamFailure if the stream does not exist.
     * @return the records for the stream in insertion order.
     */
    private List<EventRecord> getEventStream(String streamName) {
        if (streamNamesIndex.contains(streamName)) {
            return storage.stream()
                    .filter(record -> record.streamName.equals(streamName))
                    .toList();
        }
        throw new NoSuchStreamFailure(streamName);
    }

    private void save(String streamName, EventRecord record) {
        storage.add(record);
        streamNamesIndex.add(streamName);
        lastUpdateAt = Instant.now();
    }

    private record EventRecord(
            String streamName, String eventId, String eventType, String eventContent, long version, Instant timestamp) {
        private StoredRecord toStoredRecord() {
            return new StoredRecord(streamName, eventId, eventType, eventContent, version, timestamp);
        }
    }
}
