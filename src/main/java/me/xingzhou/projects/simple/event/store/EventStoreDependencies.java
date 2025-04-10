package me.xingzhou.projects.simple.event.store;

import me.xingzhou.projects.simple.event.store.entities.EventTypesExtractor;
import me.xingzhou.projects.simple.event.store.eventsmapper.ServiceLoaderEventTypeConverter;
import me.xingzhou.projects.simple.event.store.internal.tooling.EntityEventApplier;
import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.serializer.adapters.JacksonEventSerializer;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;
import me.xingzhou.projects.simple.event.store.storage.InMemoryEventStorage;

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
