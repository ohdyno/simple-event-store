package me.xingzhou.simple.event.store.storage;

import java.util.List;

public record VersionedRecords(List<StoredRecord> records, StoredRecord latestRecord) {}
