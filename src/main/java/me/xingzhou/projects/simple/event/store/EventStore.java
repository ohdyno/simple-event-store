package me.xingzhou.projects.simple.event.store;

import me.xingzhou.projects.simple.event.store.entities.Aggregate;
import me.xingzhou.projects.simple.event.store.entities.Projection;
import me.xingzhou.projects.simple.event.store.failures.StaleStateFailure;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;
import me.xingzhou.projects.simple.event.store.storage.StoredRecord;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.StaleVersionFailure;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class EventStore {

    public static EventStore build(EventStoreDependencies dependencies) {
        return new EventStore(dependencies);
    }

    private final EventStoreDependencies dependencies;

    private final Sinks.Many<StoredRecord> sinks = Sinks.many().replay().latest();

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
                        projection.lastRecordId().id(),
                        EventStorage.Constants.Ids.MAX,
                        projection.streamNames().stream().map(StreamName::value).toList(),
                        eventTypes);
        records.records().stream()
                .map(record -> EventRecord.extract(
                        record, dependencies.serializer().deserialize(record.eventType(), record.eventContent())))
                .forEach(record -> dependencies.applier().apply(record, projection));
        projection.setLastRecordId(new RecordId(records.latestRecord().id()));
        projection.setLastUpdatedOn(records.latestRecord().insertedOn());
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
        aggregate.setVersion(new Version(records.latestRecord().version()));
        return aggregate;
    }

    public Flux<StoredRecord> publisher() {
        return sinks.asFlux();
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
            sinks.tryEmitNext(record);
            return aggregate;
        } catch (DuplicateEventStreamFailure | StaleVersionFailure failure) {
            throw new StaleStateFailure();
        }
    }
}
