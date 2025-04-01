package me.xingzhou.projects.simple.event.store;

import java.util.List;

public record TimestampedEvents(List<DeserializedRecord> events, java.time.Instant timestamp) {}
