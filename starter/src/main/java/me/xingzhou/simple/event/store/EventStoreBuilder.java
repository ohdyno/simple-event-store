package me.xingzhou.simple.event.store;

import java.util.Map;
import me.xingzhou.simple.event.store.enrich.EntityEventApplier;
import me.xingzhou.simple.event.store.enrich.EventTypesExtractor;
import me.xingzhou.simple.event.store.event.converter.MapBackedEventTypeConverter;
import me.xingzhou.simple.event.store.serializer.adapters.JacksonEventSerializer;
import me.xingzhou.simple.event.store.storage.adapters.InMemoryEventStorage;

public class EventStoreBuilder {
    public static EventStore buildWithDefaults(Map<String, Class<? extends Event>> mapping) {
        var converter = new MapBackedEventTypeConverter(mapping);
        var extractor = new EventTypesExtractor(converter);
        var serializer = new JacksonEventSerializer(converter);
        var storage = new InMemoryEventStorage();
        var applier = new EntityEventApplier(extractor);
        return EventStore.build(new EventStoreDependencies(storage, serializer, extractor, applier));
    }
}
