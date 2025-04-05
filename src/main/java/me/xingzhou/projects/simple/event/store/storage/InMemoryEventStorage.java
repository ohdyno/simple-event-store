package me.xingzhou.projects.simple.event.store.storage;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        save(streamName, new EventRecord(streamName, eventId, eventType, eventContent, Instant.now()));
        return initialVersion();
    }

    @Override
    public long initialVersion() {
        return 0L;
    }

    private void save(String streamName, EventRecord record) {
        storage.add(record);
        streamNamesIndex.add(streamName);
    }

    private record EventRecord(
            String streamName, String eventId, String eventType, String eventContent, Instant timestamp) {}
}
