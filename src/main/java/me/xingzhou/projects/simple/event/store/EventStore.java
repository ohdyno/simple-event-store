package me.xingzhou.projects.simple.event.store;

import static me.xingzhou.projects.simple.event.store.internal.tooling.EntityEventApplier.apply;

import me.xingzhou.projects.simple.event.store.entities.Aggregate;
import me.xingzhou.projects.simple.event.store.entities.Projection;
import me.xingzhou.projects.simple.event.store.failures.StaleStateFailure;
import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.StaleVersionFailure;

public class EventStore {
    public static EventStore build(EventStorage storage, EventSerializer serializer) {
        return new EventStore(storage, serializer);
    }

    private final EventStorage storage;

    private final EventSerializer serializer;

    private EventStore(EventStorage storage, EventSerializer serializer) {
        this.storage = storage;
        this.serializer = serializer;
    }

    public void enrich(Projection projection) {
        var eventTypes = serializer.extractDefinedEventsFromApplyMethods(projection);
        var records = storage.retrieveEvents(
                projection.lastEventId().id(),
                EventStorage.Constants.Ids.MAX,
                projection.streamNames().stream().map(StreamName::value).toList(),
                eventTypes);
        records.records().stream()
                .map(record -> EventRecord.extract(record, serializer))
                .forEach(record -> apply(record, projection));
    }

    public <T extends Aggregate> void enrich(T aggregate) {
        var eventTypes = serializer.extractDefinedEventsFromApplyMethods(aggregate);
        var records = storage.retrieveEvents(
                aggregate.streamName().value(),
                eventTypes,
                aggregate.version().value(),
                EventStorage.Constants.Versions.MAX);
        records.records().stream()
                .map(record -> EventRecord.extract(record, serializer))
                .forEach(record -> apply(record, aggregate));
    }

    public void save(Event event, Aggregate aggregate) {
        try {
            var serialized = serializer.serialize(event);
            var record = storage.appendEvent(
                    aggregate.streamName().value(),
                    aggregate.version().value(),
                    serialized.eventType(),
                    serialized.eventJson());
            aggregate.setVersion(new Version(record.version()));
        } catch (DuplicateEventStreamFailure | StaleVersionFailure failure) {
            throw new StaleStateFailure();
        }
    }
}
