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

    @Override
    public long createStream(
            @Nonnull String streamName,
            @Nonnull String eventId,
            @Nonnull String eventType,
            @Nonnull String eventContent) {
        if (streamNamesIndex.contains(streamName)) {
            throw new DuplicateEventStreamFailure();
        }
        var record = new EventRecord(streamName, eventId, eventType, eventContent, newStreamVersion(), Instant.now());
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
        if (isCurrent(currentVersion, streamName)) {
            var record =
                    new EventRecord(streamName, eventId, eventType, eventContent, currentVersion + 1, Instant.now());
            save(streamName, record);
            return record.version();
        }
        throw new StaleVersionFailure();
    }

    private boolean isCurrent(long currentVersion, String streamName) {
        return getEventStream(streamName).size() == currentVersion;
    }

    @Override
    public @Nonnull VersionedRecords retrieveEvents(
            @Nonnull String streamName, @Nonnull List<String> eventTypes, long beginVersion, long endVersion) {
        var eventStream = getEventStream(streamName);
        var version = eventStream.size();
        var records = eventStream.stream()
                .filter(event -> shouldIncludeEvent(eventTypes, event, beginVersion, endVersion))
                .map(EventRecord::toStoredRecord)
                .toList();
        return new VersionedRecords(records, version);
    }

    private static boolean shouldIncludeEvent(
            List<String> eventTypes, EventRecord event, long beginVersion, long endVersion) {
        var isCorrectType = eventTypes.isEmpty() || eventTypes.contains(event.eventType());
        var isWithinRange = beginVersion < event.version() && event.version() < endVersion;
        return isCorrectType && isWithinRange;
    }

    private List<EventRecord> getEventStream(String streamName) {
        if (streamNamesIndex.contains(streamName)) {
            return storage.stream()
                    .filter(record -> record.streamName.equals(streamName))
                    .toList();
        }
        throw new NoSuchStreamFailure(streamName);
    }

    @Override
    public long undefinedVersion() {
        return 0;
    }

    @Override
    public long newStreamVersion() {
        return 1;
    }

    private void save(String streamName, EventRecord record) {
        storage.add(record);
        streamNamesIndex.add(streamName);
    }

    private record EventRecord(
            String streamName, String eventId, String eventType, String eventContent, long version, Instant timestamp) {
        private StoredRecord toStoredRecord() {
            return new StoredRecord(eventType, eventContent, streamName, version);
        }
    }
}
