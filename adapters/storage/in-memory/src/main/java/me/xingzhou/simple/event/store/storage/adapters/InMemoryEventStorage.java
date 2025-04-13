package me.xingzhou.simple.event.store.storage.adapters;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.*;
import me.xingzhou.simple.event.store.storage.EventStorage;
import me.xingzhou.simple.event.store.storage.EventStorage.Constants.Ids;
import me.xingzhou.simple.event.store.storage.EventStorage.Constants.Versions;
import me.xingzhou.simple.event.store.storage.StoredRecord;
import me.xingzhou.simple.event.store.storage.TimestampedRecords;
import me.xingzhou.simple.event.store.storage.VersionedRecords;
import me.xingzhou.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.simple.event.store.storage.failures.NoSuchStreamFailure;
import me.xingzhou.simple.event.store.storage.failures.StaleVersionFailure;

public class InMemoryEventStorage implements EventStorage {
    private final List<StoredRecord> storage = new ArrayList<>();

    private final Set<String> streamNamesIndex = new HashSet<>();

    @Override
    public @Nonnull StoredRecord appendEvent(
            @Nonnull String streamName, long currentVersion, @Nonnull String eventType, @Nonnull String eventContent) {
        if (isCreateStreamRequest(currentVersion)) {
            return createStream(streamName, eventType, eventContent);
        }
        return appendToStream(streamName, currentVersion, eventType, eventContent);
    }

    @Override
    public @Nonnull VersionedRecords retrieveEvents(
            @Nonnull String streamName,
            @Nonnull Collection<String> eventTypes,
            long exclusiveStartVersion,
            long inclusiveEndVersion) {
        var eventStream = getEventStream(streamName);
        var latestRecord = eventStream.getLast();
        var records = eventStream.stream()
                .filter(event -> shouldIncludeEvent(event, eventTypes, exclusiveStartVersion, inclusiveEndVersion))
                .toList();
        return new VersionedRecords(records, latestRecord);
    }

    @Override
    public @Nonnull TimestampedRecords retrieveEvents(
            long exclusiveStartId,
            long inclusiveEndId,
            @Nonnull Collection<String> streamNames,
            @Nonnull Collection<String> eventTypes) {
        var latestRecord = getLatestRecord();
        var records = storage.stream()
                .filter(event -> shouldIncludeEvent(event, exclusiveStartId, inclusiveEndId, streamNames, eventTypes))
                .toList();
        return new TimestampedRecords(records, latestRecord);
    }

    private StoredRecord appendToStream(String streamName, long currentVersion, String eventType, String eventContent) {
        if (getCurrentVersion(streamName) == currentVersion) {
            var record = new StoredRecord(
                    createId(), streamName, eventType, eventContent, currentVersion + 1, Instant.now());
            save(streamName, record);
            return record;
        }
        throw new StaleVersionFailure();
    }

    private long createId() {
        return storage.size() + Ids.START;
    }

    private StoredRecord createStream(String streamName, String eventType, String eventContent) {
        if (streamNamesIndex.contains(streamName)) {
            throw new DuplicateEventStreamFailure();
        }
        var record =
                new StoredRecord(createId(), streamName, eventType, eventContent, Versions.NEW_STREAM, Instant.now());
        save(streamName, record);
        return record;
    }

    /**
     * @param streamName is the name of a stream within {@link #storage}.
     * @throws NoSuchStreamFailure if the stream does not exist.
     * @return the current version for the stream, accounting for the value of {@link Versions#NEW_STREAM}
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
                    .sorted(Comparator.comparingLong(StoredRecord::version))
                    .toList();
        }
        throw new NoSuchStreamFailure(streamName);
    }

    private StoredRecord getLatestRecord() {
        var records = storage.stream()
                .sorted(Comparator.comparingLong(StoredRecord::id))
                .toList();
        if (records.isEmpty()) {
            return StoredRecord.emptyRecord();
        }
        return records.getLast();
    }

    private boolean isCreateStreamRequest(long currentVersion) {
        return currentVersion == Versions.UNDEFINED_STREAM;
    }

    private void save(String streamName, StoredRecord record) {
        storage.add(record);
        streamNamesIndex.add(streamName);
    }

    private boolean shouldIncludeEvent(
            StoredRecord event, Collection<String> eventTypes, long exclusiveStartVersion, long inclusiveEndVersion) {
        var isCorrectType = eventTypes.isEmpty() || eventTypes.contains(event.eventType());
        var isWithinRange = exclusiveStartVersion < event.version() && event.version() <= inclusiveEndVersion;
        return isCorrectType && isWithinRange;
    }

    private boolean shouldIncludeEvent(
            StoredRecord event,
            long exclusiveStartId,
            long inclusiveEndId,
            Collection<String> streamNames,
            Collection<String> eventTypes) {
        var isInStreams = streamNames.isEmpty() || streamNames.contains(event.streamName());
        var isCorrectType = eventTypes.isEmpty() || eventTypes.contains(event.eventType());
        var isWithinRange = exclusiveStartId < event.id() && event.id() <= inclusiveEndId;
        return isInStreams && isCorrectType && isWithinRange;
    }
}
