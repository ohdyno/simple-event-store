package me.xingzhou.simple.event.store.serializer;

import me.xingzhou.simple.event.store.Event;

/** Interface for serializing and deserializing events to and from JSON. */
public interface EventSerializer {
    /**
     * Deserializes an event from its JSON representation.
     *
     * @param eventType the string type of the event to deserialize
     * @param eventJson the JSON content of the event
     * @return the deserialized event object
     * @throws UnknownEventTypeFailure if the eventType is unknown
     */
    Event deserialize(String eventType, String eventJson);

    /**
     * Serializes an event to its JSON representation.
     *
     * @param event the event to serialize
     * @return the serialized event containing type and JSON content
     */
    SerializedEvent serialize(Event event);
}
