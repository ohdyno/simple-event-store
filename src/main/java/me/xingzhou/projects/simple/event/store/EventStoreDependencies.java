package me.xingzhou.projects.simple.event.store;

import me.xingzhou.projects.simple.event.store.entities.EventTypesExtractor;
import me.xingzhou.projects.simple.event.store.eventsmapper.EventTypeConverter;
import me.xingzhou.projects.simple.event.store.internal.tooling.EntityEventApplier;
import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;

public record EventStoreDependencies(
        EventStorage storage,
        EventSerializer serializer,
        EventTypeConverter converter,
        EventTypesExtractor extractor,
        EntityEventApplier applier) {}
