package me.xingzhou.simple.event.store.enrich;

import java.util.List;
import java.util.function.Predicate;
import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.entities.EventSourceEntity;
import me.xingzhou.simple.event.store.event.converter.EventTypeConverter;

public class EventNamesExtractor {
    private final EventTypeConverter converter;
    private final EventTypeExtractor eventTypeExtractor;

    public EventNamesExtractor(EventTypeConverter converter) {
        this.converter = converter;
        this.eventTypeExtractor = new EventTypeExtractor();
    }

    public List<String> extract(EventSourceEntity entity) {
        return eventTypeExtractor.extractTypes(entity).stream()
                .filter(Predicate.not(klass -> klass.equals(Event.class)))
                .map(converter::convert)
                .toList();
    }
}
