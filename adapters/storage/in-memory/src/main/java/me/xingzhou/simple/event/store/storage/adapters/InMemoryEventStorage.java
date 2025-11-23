package me.xingzhou.simple.event.store.storage.adapters;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.*;
import java.util.function.Supplier;
import me.xingzhou.simple.event.store.storage.EventStorage;
import me.xingzhou.simple.event.store.storage.EventStorage.Constants.Ids;
import me.xingzhou.simple.event.store.storage.EventStorage.Constants.Versions;
import me.xingzhou.simple.event.store.storage.RetrievedRecords;
import me.xingzhou.simple.event.store.storage.StoredRecord;
import me.xingzhou.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.simple.event.store.storage.failures.NoSuchStreamFailure;
import me.xingzhou.simple.event.store.storage.failures.StaleVersionFailure;

public class InMemoryEventStorage implements EventStorage {
    private final List<StoredRecord> storage = new ArrayList<>();

    private final Set<String> streamNamesIndex = new HashSet<>();

    private final Supplier<Instant> now;

    public InMemoryEventStorage() {
        this(Instant::now);
    }

    public InMemoryEventStorage(Supplier<Instant> now) {
        this.now = now;
    }

    @Override
    public @Nonnull StoredRecord appendEvent(
            @Nonnull String streamName, long currentVersion, @Nonnull String eventType, @Nonnull String eventContent) {
        if (isCreateStreamRequest(currentVersion)) {
            return createStream(streamName, eventType, eventContent);
        }
        return appendToStream(streamName, currentVersion, eventType, eventContent);
    }

    @Override
    public @Nonnull RetrievedRecords retrieveEvents(
            @Nonnull String streamName,
            @Nonnull Collection<String> eventTypes,
            long exclusiveStartVersion,
            long inclusiveEndVersion) {
        var eventStream = getEventStream(streamName);
        var latestRecord = eventStream.getLast();
        var records = eventStream.stream()
                .filter(event -> shouldIncludeEvent(event, eventTypes, exclusiveStartVersion, inclusiveEndVersion))
                .toList();
        return new RetrievedRecords(records, latestRecord);
    }

    @Override
    public @Nonnull RetrievedRecords retrieveEvents(
            long exclusiveStartId,
            long inclusiveEndId,
            @Nonnull Collection<String> streamNames,
            @Nonnull Collection<String> eventTypes) {
        var latestRecord = getLatestRecord();
        var records = storage.stream()
                .filter(event -> shouldIncludeEvent(event, exclusiveStartId, inclusiveEndId, streamNames, eventTypes))
                .toList();
        return new RetrievedRecords(records, latestRecord);
    }

    private StoredRecord appendToStream(String streamName, long currentVersion, String eventType, String eventContent) {
        if (getCurrentVersion(streamName) == currentVersion) {
            var record =
                    new StoredRecord(createId(), streamName, eventType, eventContent, currentVersion + 1, now.get());
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
        var record = new StoredRecord(createId(), streamName, eventType, eventContent, Versions.NEW_STREAM, now.get());
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
