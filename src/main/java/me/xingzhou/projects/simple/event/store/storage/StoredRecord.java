package me.xingzhou.projects.simple.event.store.storage;

import java.time.Instant;

public record StoredRecord(
        long eventId, String streamName, String eventType, String eventContent, long version, Instant timestamp) {}
