package me.xingzhou.projects.simple.event.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.xingzhou.projects.simple.event.store.entities.ProjectionRecorder;
import me.xingzhou.projects.simple.event.store.entities.TestAggregate;
import me.xingzhou.projects.simple.event.store.events.TestEvent;
import me.xingzhou.projects.simple.event.store.failures.StaleStateFailure;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EventStoreTest {

    private EventStore store;

    @Test
    void enrichAProjection() {
        var event = new TestEvent("event-id");
        store.save(event, new TestAggregate());

        var recorder = store.enrich(new ProjectionRecorder());

        assertThat(recorder.appliedEvents()).hasSize(1);
        assertThat(recorder.appliedEvents().getFirst()).isEqualTo(event);
        assertThat(recorder.appliedEvents().getFirst()).isNotSameAs(event);
    }

    @Test
    void enrichAnAggregateFromAStream() {
        var event = new TestEvent("event-id");
        store.save(event, new TestAggregate());

        var recorder = store.enrich(new TestAggregate());

        assertThat(recorder.appliedEvents()).hasSize(1);
        assertThat(recorder.appliedEvents().getFirst()).isEqualTo(event);
        assertThat(recorder.appliedEvents().getFirst()).isNotSameAs(event);
        assertThat(recorder.version().value()).isEqualTo(EventStorage.Constants.Versions.NEW_STREAM);
    }

    @Test
    void saveAnEvent() {
        var event = new TestEvent();
        var aggregate = store.save(event, new TestAggregate());
        assertThat(aggregate.version().value()).isEqualTo(EventStorage.Constants.Versions.NEW_STREAM);
    }

    @Test
    void saveAnEventWithAnAggregateWithStaleVersion() {
        var event = new TestEvent();
        store.save(event, new TestAggregate());

        assertThatThrownBy(() -> store.save(event, new TestAggregate())).isInstanceOf(StaleStateFailure.class);
    }

    @BeforeEach
    void setUp() {
        this.store = EventStore.build(EventStoreDependencies.buildWithInMemoryStorage());
    }
}
