package me.xingzhou.projects.simple.event.store.storage;

import java.util.List;

public record VersionedRecords(List<StoredRecord> records, String version) {}
