package me.xingzhou.projects.simple.event.store.storage;

import java.util.List;

public record TimestampedRecords(List<StoredRecord> records, StoredRecord latestRecord) {}
