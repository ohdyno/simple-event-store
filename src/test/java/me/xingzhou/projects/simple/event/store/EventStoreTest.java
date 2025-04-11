package me.xingzhou.projects.simple.event.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import me.xingzhou.projects.simple.event.store.entities.ProjectionRecorder;
import me.xingzhou.projects.simple.event.store.entities.TestAggregate;
import me.xingzhou.projects.simple.event.store.events.TestEvent;
import me.xingzhou.projects.simple.event.store.failures.StaleStateFailure;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

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

        StepVerifier.create(store.publisher())
                .assertNext(record -> {
                    assertThat(recorder.lastRecordId().id()).isEqualTo(record.id());
                    assertThat(recorder.lastUpdatedOn()).isEqualTo(record.insertedOn());
                })
                .thenCancel()
                .verify();
    }

    @Test
    void enrichAnAggregateFromAStream() {
        var event = new TestEvent("event-id");
        store.save(event, new TestAggregate());

        var recorder = store.enrich(new TestAggregate());

        assertThat(recorder.appliedEvents()).hasSize(1);
        assertThat(recorder.appliedEvents().getFirst()).isEqualTo(event);
        assertThat(recorder.appliedEvents().getFirst()).isNotSameAs(event);
        StepVerifier.create(store.publisher())
                .assertNext(record -> assertThat(recorder.version().value()).isEqualTo(record.version()))
                .thenCancel()
                .verify();
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
        StepVerifier.setDefaultTimeout(Duration.ofMillis(100));
    }

    @Test
    void subscribe() {
        var event = new TestEvent();

        store.save(event, new TestAggregate());

        StepVerifier.create(store.publisher())
                .assertNext(record -> assertThat(record.id()).isEqualTo(EventStorage.Constants.Ids.START))
                .thenCancel()
                .verify();
    }
}
