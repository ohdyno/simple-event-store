package me.xingzhou.projects.simple.event.store.serializer;

import java.util.List;
import me.xingzhou.projects.simple.event.store.Event;

public interface EventSerializer {
    SerializedEvent serialize(Event event);

    String getTypeName(Class<? extends Event> klass);

    /** @throws UnknownEventTypeFailure if the eventType is unknown */
    Event deserialize(String eventType, String eventJson);

    List<String> extractDefinedEventsFromApplyMethods(Object object);
}
