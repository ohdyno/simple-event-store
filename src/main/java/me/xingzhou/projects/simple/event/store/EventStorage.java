package me.xingzhou.projects.simple.event.store;

public interface EventStorage {
  String createStream(String streamName, String eventId, String eventType, String eventJson);
}
