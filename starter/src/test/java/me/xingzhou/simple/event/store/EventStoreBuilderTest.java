package me.xingzhou.simple.event.store;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import me.xingzhou.simple.event.store.entities.BaseAggregate;
import me.xingzhou.simple.event.store.ids.StreamName;
import org.junit.jupiter.api.Test;

class EventStoreBuilderTest {
    @Test
    void successfullyBuildsADefaultEventStore() {
        var store = EventStoreBuilder.buildWithDefaults(Map.of("TestEvent", TestEvent.class));
        var event = new TestEvent("event-id");
        store.save(event, new TestAggregate());

        var recorder = store.enrich(new TestAggregate());

        assertThat(recorder.appliedEvents()).hasSize(1);
        assertThat(recorder.appliedEvents().getFirst()).isEqualTo(event);
        assertThat(recorder.appliedEvents().getFirst()).isNotSameAs(event);
    }

    private record TestEvent(String id) implements Event {}

    public static class TestAggregate extends BaseAggregate {
        private final List<Event> appliedEvents = new ArrayList<>();

        @Override
        public StreamName streamName() {
            return new StreamName("test-aggregate-stream");
        }

        public void apply(Event event) {
            appliedEvents.add(event);
        }

        private List<Event> appliedEvents() {
            return appliedEvents;
        }
    }
}
