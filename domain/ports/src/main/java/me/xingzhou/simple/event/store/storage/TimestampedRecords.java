package me.xingzhou.simple.event.store.storage;

import java.util.List;

public record TimestampedRecords(List<StoredRecord> records, StoredRecord latestRecord) {}
