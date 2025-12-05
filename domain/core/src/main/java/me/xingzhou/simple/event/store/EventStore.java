package me.xingzhou.simple.event.store;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import me.xingzhou.simple.event.store.enrich.EventRecord;
import me.xingzhou.simple.event.store.entities.Aggregate;
import me.xingzhou.simple.event.store.entities.EventSourceEntity;
import me.xingzhou.simple.event.store.entities.Projection;
import me.xingzhou.simple.event.store.failures.StaleStateFailure;
import me.xingzhou.simple.event.store.failures.StreamNotFoundFailure;
import me.xingzhou.simple.event.store.ids.RecordId;
import me.xingzhou.simple.event.store.ids.StreamName;
import me.xingzhou.simple.event.store.ids.Version;
import me.xingzhou.simple.event.store.storage.EventStorage;
import me.xingzhou.simple.event.store.storage.RetrievedRecords;
import me.xingzhou.simple.event.store.storage.StoredRecord;
import me.xingzhou.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.simple.event.store.storage.failures.NoSuchStreamFailure;
import me.xingzhou.simple.event.store.storage.failures.StaleVersionFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class EventStore {

    private static final Logger log = LoggerFactory.getLogger(EventStore.class);

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
        return EntityEnricher.enrich(projection)
                .with(eventTypes -> dependencies
                        .storage()
                        .retrieveEvents(
                                projection.lastRecordId().id(),
                                EventStorage.Constants.Ids.MAX,
                                projection.streamNames().stream()
                                        .map(StreamName::value)
                                        .toList(),
                                eventTypes))
                .onSuccess(records -> {
                    projection.setLastRecordId(
                            new RecordId(records.latestRecord().id()));
                    projection.setLastUpdatedOn(records.latestRecord().insertedOn());
                })
                .perform(dependencies);
    }

    public <T extends Aggregate> T enrich(T aggregate, EventStoreDependencies dependencies) {
        try {
            return EntityEnricher.enrich(aggregate)
                    .with(eventTypes -> dependencies
                            .storage()
                            .retrieveEvents(
                                    aggregate.streamName().value(),
                                    eventTypes,
                                    aggregate.version().value(),
                                    EventStorage.Constants.Versions.MAX))
                    .onSuccess(records -> aggregate.setVersion(
                            new Version(records.latestRecord().version())))
                    .perform(dependencies);
        } catch (NoSuchStreamFailure e) {
            throw new StreamNotFoundFailure(e);
        }
    }

    public Flux<StoredRecord> publisher() {
        return sinks.asFlux();
    }

    public <T extends Aggregate> T save(Event event, T aggregate) {
        return save(event, aggregate, dependencies);
    }

    public <T extends Aggregate> T save(Event event, T aggregate, EventStoreDependencies dependencies) {
        try {
            log.debug("save event {} for aggregate {}", event, aggregate);
            var serialized = dependencies.serializer().serialize(event);
            var record = dependencies
                    .storage()
                    .appendEvent(
                            aggregate.streamName().value(),
                            aggregate.version().value(),
                            serialized.eventType(),
                            serialized.eventJson());
            dependencies.applier().apply(EventRecord.extract(record, event), aggregate);
            aggregate.setVersion(new Version(record.version()));
            aggregate.handleEnrichedSuccessfully();
            publish(record);
            return aggregate;
        } catch (DuplicateEventStreamFailure | StaleVersionFailure failure) {
            throw new StaleStateFailure();
        }
    }

    private void publish(StoredRecord record) {
        log.debug("publish event record: {}", record);
        sinks.tryEmitNext(record);
    }

    private static final class EntityEnricher<T extends EventSourceEntity> {
        private static <T extends EventSourceEntity> EntityEnricher<T> enrich(T entity) {
            return new EntityEnricher<>(entity);
        }

        private final T entity;
        private Function<List<String>, RetrievedRecords> recordsRetriever;

        private Consumer<RetrievedRecords> onSuccessHandler;

        private EntityEnricher(T entity) {
            this.entity = entity;
        }

        private EntityEnricher<T> onSuccess(Consumer<RetrievedRecords> onSuccess) {
            this.onSuccessHandler = onSuccess;
            return this;
        }

        private T perform(EventStoreDependencies dependencies) {
            log.info("enriching {}", entity);
            var eventTypes = dependencies.extractor().extract(entity);
            var records = recordsRetriever.apply(eventTypes);
            int recordsSize = records.records().size();
            log.info("retrieved {} events", recordsSize);
            if (recordsSize > 0) {
                records.records().stream()
                        .map(record -> EventRecord.extract(
                                record,
                                dependencies.serializer().deserialize(record.eventType(), record.eventContent())))
                        .forEach(record -> dependencies.applier().apply(record, entity));
                log.info("applied {} events to {}", recordsSize, entity);
                entity.handleEnrichedSuccessfully();
            }
            onSuccessHandler.accept(records);
            return entity;
        }

        private EntityEnricher<T> with(Function<List<String>, RetrievedRecords> recordsRetriever) {
            this.recordsRetriever = recordsRetriever;
            return this;
        }
    }
}
