package me.xingzhou.simple.event.store;

import java.util.Map;
import java.util.Objects;
import me.xingzhou.simple.event.store.enrich.EntityEventApplier;
import me.xingzhou.simple.event.store.enrich.EventTypesExtractor;
import me.xingzhou.simple.event.store.event.converter.EventTypeConverter;
import me.xingzhou.simple.event.store.event.converter.MapBackedEventTypeConverter;
import me.xingzhou.simple.event.store.event.converter.ServiceLoaderEventTypeConverter;
import me.xingzhou.simple.event.store.serializer.adapters.JacksonEventSerializer;
import me.xingzhou.simple.event.store.storage.EventStorage;
import me.xingzhou.simple.event.store.storage.adapters.InMemoryEventStorage;

public class EventStoreBuilder {
    public static EventStore buildWithDefaults(Map<String, Class<? extends Event>> mapping) {
        return new EventStoreBuilder()
                .withEventTypeConverter(new MapBackedEventTypeConverter(mapping))
                .build();
    }

    private EventTypeConverter eventTypeConverter;

    private EventStorage storage;

    public EventStore build() {
        var converter = Objects.requireNonNullElse(eventTypeConverter, new ServiceLoaderEventTypeConverter());
        var extractor = new EventTypesExtractor(converter);
        var serializer = new JacksonEventSerializer(converter);
        var storage = Objects.requireNonNullElse(this.storage, new InMemoryEventStorage());
        var applier = new EntityEventApplier(extractor);
        return EventStore.build(new EventStoreDependencies(storage, serializer, extractor, applier));
    }

    public EventStoreBuilder withEventTypeConverter(EventTypeConverter eventTypeConverter) {
        this.eventTypeConverter = eventTypeConverter;
        return this;
    }

    public EventStoreBuilder withStorage(EventStorage storage) {
        this.storage = storage;
        return this;
    }
}
