package me.xingzhou.projects.simple.event.store;

import me.xingzhou.projects.simple.event.store.entities.Aggregate;
import me.xingzhou.projects.simple.event.store.entities.Projection;
import me.xingzhou.projects.simple.event.store.failures.StaleStateFailure;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.StaleVersionFailure;

public class EventStore {

    public static EventStore build(EventStoreDependencies dependencies) {
        return new EventStore(dependencies);
    }

    private final EventStoreDependencies dependencies;

    private EventStore(EventStoreDependencies dependencies) {
        this.dependencies = dependencies;
    }

    public <T extends Projection> T enrich(T projection) {
        return enrich(projection, dependencies);
    }

    public <T extends Aggregate> T enrich(T aggregate) {
        return enrich(aggregate, dependencies);
    }

    public <T extends Projection> T enrich(T projection, EventStoreDependencies dependencies) {
        var eventTypes = dependencies.extractor().extract(projection);
        var records = dependencies
                .storage()
                .retrieveEvents(
                        projection.lastEventId().id(),
                        EventStorage.Constants.Ids.MAX,
                        projection.streamNames().stream().map(StreamName::value).toList(),
                        eventTypes);
        records.records().stream()
                .map(record -> EventRecord.extract(
                        record, dependencies.serializer().deserialize(record.eventType(), record.eventContent())))
                .forEach(record -> dependencies.applier().apply(record, projection));
        return projection;
    }

    public <T extends Aggregate> T enrich(T aggregate, EventStoreDependencies dependencies) {
        var eventTypes = dependencies.extractor().extract(aggregate);
        var records = dependencies
                .storage()
                .retrieveEvents(
                        aggregate.streamName().value(),
                        eventTypes,
                        aggregate.version().value(),
                        EventStorage.Constants.Versions.MAX);
        records.records().stream()
                .map(record -> {
                    var deserialized = dependencies.serializer().deserialize(record.eventType(), record.eventContent());
                    return EventRecord.extract(record, deserialized);
                })
                .forEach(record -> dependencies.applier().apply(record, aggregate));
        return aggregate;
    }

    public <T extends Aggregate> T save(Event event, T aggregate) {
        return save(event, aggregate, dependencies);
    }

    public <T extends Aggregate> T save(Event event, T aggregate, EventStoreDependencies dependencies) {
        try {
            var serialized = dependencies.serializer().serialize(event);
            var record = dependencies
                    .storage()
                    .appendEvent(
                            aggregate.streamName().value(),
                            aggregate.version().value(),
                            serialized.eventType(),
                            serialized.eventJson());
            aggregate.setVersion(new Version(record.version()));
            return aggregate;
        } catch (DuplicateEventStreamFailure | StaleVersionFailure failure) {
            throw new StaleStateFailure();
        }
    }
}
