package me.xingzhou.projects.simple.event.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.xingzhou.projects.simple.event.store.entities.EventTypesExtractor;
import me.xingzhou.projects.simple.event.store.entities.ProjectionRecorder;
import me.xingzhou.projects.simple.event.store.entities.TestAggregate;
import me.xingzhou.projects.simple.event.store.events.TestEvent;
import me.xingzhou.projects.simple.event.store.eventsmapper.ServiceLoaderEventTypeConverter;
import me.xingzhou.projects.simple.event.store.failures.StaleStateFailure;
import me.xingzhou.projects.simple.event.store.internal.tooling.EntityEventApplier;
import me.xingzhou.projects.simple.event.store.serializer.adapters.JacksonEventSerializer;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;
import me.xingzhou.projects.simple.event.store.storage.InMemoryEventStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventStoreTest {

    private EventStore store;

    @Test
    void enrichAProjection() {
        var event = new TestEvent("event-id");
        var aggregate = new TestAggregate();
        store.save(event, aggregate);

        var recorder = new ProjectionRecorder();
        store.enrich(recorder);

        assertThat(recorder.appliedEvents()).hasSize(1);
        assertThat(recorder.appliedEvents().getFirst()).isEqualTo(event);
        assertThat(recorder.appliedEvents().getFirst()).isNotSameAs(event);
    }

    @Test
    void enrichAnAggregateFromAStream() {
        var event = new TestEvent("event-id");
        var aggregate = new TestAggregate();
        store.save(event, aggregate);

        var recorder = new TestAggregate();
        store.enrich(recorder);

        assertThat(recorder.appliedEvents()).hasSize(1);
        assertThat(recorder.appliedEvents().getFirst()).isEqualTo(event);
        assertThat(recorder.appliedEvents().getFirst()).isNotSameAs(event);
    }

    @Test
    void saveAnEvent() {
        var event = new TestEvent();
        var aggregate = new TestAggregate();
        store.save(event, aggregate);
        assertThat(aggregate.version().value()).isEqualTo(EventStorage.Constants.Versions.NEW_STREAM);
    }

    @Test
    void saveAnEventWithAnAggregateWithStaleVersion() {
        var event = new TestEvent();
        var aggregate = new TestAggregate();
        store.save(event, aggregate);

        assertThatThrownBy(() -> store.save(event, new TestAggregate())).isInstanceOf(StaleStateFailure.class);
    }

    @BeforeEach
    void setUp() {
        var converter = new ServiceLoaderEventTypeConverter();
        var extractor = new EventTypesExtractor(converter);
        var serializer = new JacksonEventSerializer(converter);
        var storage = new InMemoryEventStorage();
        var applier = new EntityEventApplier(extractor);
        this.store = EventStore.build(new EventStoreDependencies(storage, serializer, converter, extractor, applier));
    }
}
