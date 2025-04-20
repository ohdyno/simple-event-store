package me.xingzhou.simple.event.store;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import java.time.Duration;
import java.util.stream.Collectors;
import me.xingzhou.simple.event.store.enrich.EntityEventApplier;
import me.xingzhou.simple.event.store.enrich.EventTypesExtractor;
import me.xingzhou.simple.event.store.entities.ProjectionRecorder;
import me.xingzhou.simple.event.store.entities.TestAggregate;
import me.xingzhou.simple.event.store.events.TestEvent;
import me.xingzhou.simple.event.store.events.TestEventTypeConverter;
import me.xingzhou.simple.event.store.failures.StaleStateFailure;
import me.xingzhou.simple.event.store.serializer.adapters.JacksonEventSerializer;
import me.xingzhou.simple.event.store.storage.EventStorage;
import me.xingzhou.simple.event.store.storage.adapters.InMemoryEventStorage;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.scrubbers.DateScrubber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class EventStoreTest {

    private static final TestLogger logger = TestLoggerFactory.getTestLogger(EventStore.class);

    private EventStore store;

    @Test
    void enrichAProjection() {
        var event = new TestEvent("event-id");
        store.save(event, new TestAggregate());

        var recorder = store.enrich(new ProjectionRecorder());

        assertThat(recorder.isDefined()).isTrue();
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
    void logging() {
        var event = new TestEvent("event-id");
        var aggregate = new TestAggregate();

        store.save(event, aggregate);
        store.save(event, aggregate);

        store.enrich(new ProjectionRecorder());

        var loggedEvents = logger.getAllLoggingEvents().stream()
                .map(LoggingEvent::toString)
                .collect(Collectors.joining("\n"));
        Approvals.verify(
                loggedEvents, new Options().withScrubber(DateScrubber.getScrubberFor("2025-04-20T04:21:48.191772Z")));
    }

    @Test
    void saveAnEvent() {
        var event = new TestEvent();
        var aggregate = store.save(event, new TestAggregate());

        assertThat(aggregate.isDefined()).isTrue();
        assertThat(aggregate.version().value()).isEqualTo(EventStorage.Constants.Versions.NEW_STREAM);
        assertThat(aggregate.appliedEvents()).containsExactly(event);
    }

    @Test
    void saveAnEventWithAnAggregateWithStaleVersion() {
        var event = new TestEvent();
        store.save(event, new TestAggregate());

        assertThatThrownBy(() -> store.save(event, new TestAggregate())).isInstanceOf(StaleStateFailure.class);
    }

    @BeforeEach
    void setUp() {
        logger.clearAll();
        var converter = new TestEventTypeConverter();
        var extractor = new EventTypesExtractor(converter);
        var serializer = new JacksonEventSerializer(converter);
        var storage = new InMemoryEventStorage();
        var applier = new EntityEventApplier(extractor);
        this.store = EventStore.build(new EventStoreDependencies(storage, serializer, extractor, applier));
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
