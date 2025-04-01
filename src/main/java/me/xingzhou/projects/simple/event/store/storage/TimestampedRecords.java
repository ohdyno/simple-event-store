package me.xingzhou.projects.simple.event.store.storage;

import java.time.Instant;
import java.util.List;

public record TimestampedRecords(List<StoredRecord> records, Instant timestamp) {}
