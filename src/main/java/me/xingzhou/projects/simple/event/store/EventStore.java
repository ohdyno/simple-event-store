package me.xingzhou.projects.simple.event.store;

import static me.xingzhou.projects.simple.event.store.internal.tooling.EntityEventApplier.apply;

import java.util.List;
import java.util.function.Predicate;
import me.xingzhou.projects.simple.event.store.entities.Aggregate;
import me.xingzhou.projects.simple.event.store.entities.EventSourceEntity;
import me.xingzhou.projects.simple.event.store.entities.Projection;
import me.xingzhou.projects.simple.event.store.eventsmapper.EventTypeConverter;
import me.xingzhou.projects.simple.event.store.failures.StaleStateFailure;
import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.StaleVersionFailure;

public class EventStore {
    public static EventStore build(
            EventStorage storage, EventSerializer serializer, EventTypeConverter eventTypeConverter) {
        return new EventStore(storage, serializer, eventTypeConverter);
    }

    private final EventStorage storage;

    private final EventSerializer serializer;
    private final EventTypeConverter eventTypeConverter;

    private EventStore(EventStorage storage, EventSerializer serializer, EventTypeConverter eventTypeConverter) {
        this.storage = storage;
        this.serializer = serializer;
        this.eventTypeConverter = eventTypeConverter;
    }

    public void enrich(Projection projection) {
        var eventTypes = getEventTypes(projection);
        var records = storage.retrieveEvents(
                projection.lastEventId().id(),
                EventStorage.Constants.Ids.MAX,
                projection.streamNames().stream().map(StreamName::value).toList(),
                eventTypes);
        records.records().stream()
                .map(record ->
                        EventRecord.extract(record, serializer.deserialize(record.eventType(), record.eventContent())))
                .forEach(record -> apply(record, projection));
    }

    public <T extends Aggregate> void enrich(T aggregate) {
        var eventTypes = getEventTypes(aggregate);
        var records = storage.retrieveEvents(
                aggregate.streamName().value(),
                eventTypes,
                aggregate.version().value(),
                EventStorage.Constants.Versions.MAX);
        records.records().stream()
                .map(record -> {
                    var deserialized = serializer.deserialize(record.eventType(), record.eventContent());
                    return EventRecord.extract(record, deserialized);
                })
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

    private List<String> getEventTypes(EventSourceEntity entity) {
        return entity.extractEventTypesFromApplyMethods().stream()
                .filter(Predicate.not(klass -> klass.equals(Event.class)))
                .map(eventTypeConverter::convert)
                .toList();
    }
}
