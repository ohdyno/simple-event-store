package me.xingzhou.simple.event.store.serializer;

/**
 * Represents a serialized event with its type and JSON content.
 *
 * @param eventType the string representation of the event type
 * @param eventJson the JSON serialized content of the event
 */
public record SerializedEvent(String eventType, String eventJson) {}
