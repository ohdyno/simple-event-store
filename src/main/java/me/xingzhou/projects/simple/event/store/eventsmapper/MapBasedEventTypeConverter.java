package me.xingzhou.projects.simple.event.store.eventsmapper;

import java.util.Map;
import me.xingzhou.projects.simple.event.store.Event;
import me.xingzhou.projects.simple.event.store.serializer.UnknownEventTypeFailure;

public class MapBasedEventTypeConverter implements EventTypeConverter {
    private final Map<String, Class<? extends Event>> mapping;

    public MapBasedEventTypeConverter(Map<String, Class<? extends Event>> mapping) {
        this.mapping = mapping;
    }

    @Override
    public String convert(Class<?> event) {
        return mapping.entrySet().stream()
                .filter(entry -> event.equals(entry.getValue()))
                .findFirst()
                .orElseThrow(() -> new UnknownEventTypeFailure(event))
                .getKey();
    }

    @Override
    public Class<?> convert(String eventType) {
        if (mapping.containsKey(eventType)) {
            return mapping.get(eventType);
        }
        throw new UnknownEventTypeFailure(eventType);
    }
}
