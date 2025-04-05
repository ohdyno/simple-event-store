package me.xingzhou.projects.simple.event.store.storage;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.NoSuchStreamFailure;

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
            String streamName, long currentVersion, String eventId, String eventType, String eventContent) {
        var record = new EventRecord(streamName, eventId, eventType, eventContent, currentVersion + 1, Instant.now());
        save(streamName, record);
        return record.version();
    }

    @Override
    public VersionedRecords retrieveEvents(
            String streamName, List<String> eventTypes, long beginVersion, long endVersion) {
        if (streamNamesIndex.contains(streamName)) {
            var eventStream = getEventStream(streamName);
            var version = eventStream.size();
            var records = eventStream.stream().map(EventRecord::toStoredRecord).toList();
            return new VersionedRecords(records, version);
        }
        throw new NoSuchStreamFailure(streamName);
    }

    private List<EventRecord> getEventStream(String streamName) {
        return storage.stream()
                .filter(record -> record.streamName.equals(streamName))
                .toList();
    }

    @Override
    public long newStreamVersion() {
        return 0L;
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
