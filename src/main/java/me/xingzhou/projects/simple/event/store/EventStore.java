package me.xingzhou.projects.simple.event.store;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import me.xingzhou.projects.simple.event.store.entities.Aggregate;
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

    private <T extends Aggregate> void apply(EventRecord record, T aggregate) {
        handleExceptions(() -> {
            var lookup = MethodHandles.publicLookup();
            var methodType = MethodType.methodType(void.class, record.event().getClass());
            var applyMethod = lookup.findVirtual(aggregate.getClass(), "apply", methodType);
            applyMethod.invoke(aggregate, record.event());
        });
    }
}
