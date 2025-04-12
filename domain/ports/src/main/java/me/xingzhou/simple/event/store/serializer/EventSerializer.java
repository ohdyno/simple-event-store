package me.xingzhou.simple.event.store.serializer;

import me.xingzhou.simple.event.store.Event;

public interface EventSerializer {
    /** @throws UnknownEventTypeFailure if the eventType is unknown */
    Event deserialize(String eventType, String eventJson);

    SerializedEvent serialize(Event event);
}
