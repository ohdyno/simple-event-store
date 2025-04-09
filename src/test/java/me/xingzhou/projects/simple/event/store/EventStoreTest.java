package me.xingzhou.projects.simple.event.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.xingzhou.projects.simple.event.store.entities.ProjectionRecorder;
import me.xingzhou.projects.simple.event.store.entities.TestAggregate;
import me.xingzhou.projects.simple.event.store.events.TestEvent;
import me.xingzhou.projects.simple.event.store.failures.StaleStateFailure;
import me.xingzhou.projects.simple.event.store.serializer.adapters.ServiceProviderEventSerializer;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;
import me.xingzhou.projects.simple.event.store.storage.InMemoryEventStorage;
import org.junit.jupiter.api.Test;

class EventStoreTest {

    @Test
    void enrichAProjection() {
        var store = EventStore.build(new InMemoryEventStorage(), new ServiceProviderEventSerializer());
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
        var store = EventStore.build(new InMemoryEventStorage(), new ServiceProviderEventSerializer());
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
        var store = EventStore.build(new InMemoryEventStorage(), new ServiceProviderEventSerializer());
        var event = new TestEvent();
        var aggregate = new TestAggregate();
        store.save(event, aggregate);
        assertThat(aggregate.version().value()).isEqualTo(EventStorage.Constants.Versions.NEW_STREAM);
    }

    @Test
    void saveAnEventWithAnAggregateWithStaleVersion() {
        var store = EventStore.build(new InMemoryEventStorage(), new ServiceProviderEventSerializer());
        var event = new TestEvent();
        var aggregate = new TestAggregate();
        store.save(event, aggregate);

        assertThatThrownBy(() -> store.save(event, new TestAggregate())).isInstanceOf(StaleStateFailure.class);
    }
}
