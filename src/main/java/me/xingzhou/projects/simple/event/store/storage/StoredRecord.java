package me.xingzhou.projects.simple.event.store.storage;

import java.time.Instant;

public record StoredRecord(
        String streamName, String eventId, String eventType, String eventContent, long version, Instant timestamp) {}
