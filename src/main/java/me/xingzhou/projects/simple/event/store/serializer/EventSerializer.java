package me.xingzhou.projects.simple.event.store.serializer;

import me.xingzhou.projects.simple.event.store.Event;

public interface EventSerializer {
    SerializedEvent serialize(Event event);

    String getTypeName(Class<? extends Event> klass);

    Event deserialize(String eventType, String eventJson);
}
