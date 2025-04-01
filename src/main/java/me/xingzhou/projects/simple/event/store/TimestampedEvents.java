package me.xingzhou.projects.simple.event.store;

import java.time.Instant;
import java.util.List;

public record TimestampedEvents(List<DeserializedRecord> events, Instant timestamp) {}
