package me.xingzhou.simple.event.store;

import me.xingzhou.simple.event.store.event.converter.ServiceLoaderEventTypeConverter;
import me.xingzhou.simple.event.store.internal.tooling.EntityEventApplier;
import me.xingzhou.simple.event.store.internal.tooling.EventTypesExtractor;
import me.xingzhou.simple.event.store.serializer.EventSerializer;
import me.xingzhou.simple.event.store.serializer.adapters.JacksonEventSerializer;
import me.xingzhou.simple.event.store.storage.EventStorage;
import me.xingzhou.simple.event.store.storage.InMemoryEventStorage;

public record EventStoreDependencies(
        EventStorage storage, EventSerializer serializer, EventTypesExtractor extractor, EntityEventApplier applier) {
    public static EventStoreDependencies buildWithInMemoryStorage() {
        var converter = new ServiceLoaderEventTypeConverter();
        var extractor = new EventTypesExtractor(converter);
        var serializer = new JacksonEventSerializer(converter);
        var storage = new InMemoryEventStorage();
        var applier = new EntityEventApplier(extractor);
        return new EventStoreDependencies(storage, serializer, extractor, applier);
    }
}
