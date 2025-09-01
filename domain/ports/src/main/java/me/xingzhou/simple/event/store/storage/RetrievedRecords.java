package me.xingzhou.simple.event.store.storage;

import java.util.List;

/**
 * Container for retrieved event records and metadata.
 *
 * @param records the list of retrieved stored records
 * @param latestRecord the latest record in the result set
 */
public record RetrievedRecords(List<StoredRecord> records, StoredRecord latestRecord) {}
