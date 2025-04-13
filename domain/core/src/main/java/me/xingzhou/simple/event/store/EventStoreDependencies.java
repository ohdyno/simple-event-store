package me.xingzhou.simple.event.store;

import me.xingzhou.simple.event.store.enrich.EntityEventApplier;
import me.xingzhou.simple.event.store.enrich.EventTypesExtractor;
import me.xingzhou.simple.event.store.serializer.EventSerializer;
import me.xingzhou.simple.event.store.storage.EventStorage;

public record EventStoreDependencies(
        EventStorage storage, EventSerializer serializer, EventTypesExtractor extractor, EntityEventApplier applier) {}
