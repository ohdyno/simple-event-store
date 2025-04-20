package me.xingzhou.simple.event.store.storage;

import java.util.List;

public record RetrievedRecords(List<StoredRecord> records, StoredRecord latestRecord) {}
