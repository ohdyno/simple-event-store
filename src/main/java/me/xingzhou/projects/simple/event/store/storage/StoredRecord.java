package me.xingzhou.projects.simple.event.store.storage;

public record StoredRecord(String eventType, String eventContent, String streamName, long version) {}
