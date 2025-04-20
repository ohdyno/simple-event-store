package me.xingzhou.simple.event.store.event.converter;

import java.util.Map;
import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.serializer.UnknownEventTypeFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapBackedEventTypeConverter implements EventTypeConverter {
    private static final Logger log = LoggerFactory.getLogger(MapBackedEventTypeConverter.class);
    private final Map<String, Class<? extends Event>> mapping;

    public MapBackedEventTypeConverter(Map<String, Class<? extends Event>> mapping) {
        log.info("loaded mapping: {}", mapping);
        this.mapping = mapping;
    }

    @Override
    public String convert(Class<?> event) {
        log.debug("convert event: {}", event);
        return mapping.entrySet().stream()
                .filter(entry -> event.equals(entry.getValue()))
                .findFirst()
                .orElseThrow(() -> new UnknownEventTypeFailure(event))
                .getKey();
    }

    @Override
    public Class<?> convert(String eventType) {
        if (mapping.containsKey(eventType)) {
            log.debug("convert from event type: {}", eventType);
            return mapping.get(eventType);
        }
        throw new UnknownEventTypeFailure(eventType);
    }
}
