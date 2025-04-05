package me.xingzhou.projects.simple.event.store.storage;

public record StoredRecord(String eventType, String eventJson, String streamName, long version) {}
