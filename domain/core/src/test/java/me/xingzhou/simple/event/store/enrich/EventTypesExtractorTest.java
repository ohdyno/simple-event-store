package me.xingzhou.simple.event.store.enrich;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.RecordDetails;
import me.xingzhou.simple.event.store.entities.EventSourceEntity;
import me.xingzhou.simple.event.store.events.TestEvent;
import me.xingzhou.simple.event.store.events.TestEventTypeConverter;
import org.junit.jupiter.api.Test;

class EventTypesExtractorTest {

    private final EventTypesExtractor eventTypesExtractor = new EventTypesExtractor(new TestEventTypeConverter());

    @Test
    void extract() {
        assertAll(
                "Extract all relevant events from apply methods",
                () -> {
                    var entity = new EventSourceEntity() {
                        public void apply(Event event) {}
                    };
                    assertThat(eventTypesExtractor.extractTypes(entity)).containsOnly(Event.class);
                },
                () -> {
                    var entity = new EventSourceEntity() {
                        public void apply(Event event) {}

                        public void apply(TestEvent event) {}
                    };
                    assertThat(eventTypesExtractor.extractTypes(entity)).containsExactly(Event.class, TestEvent.class);
                },
                () -> {
                    var entity = new EventSourceEntity() {
                        public void apply(Event event, RecordDetails details) {}

                        public void apply(TestEvent event) {}
                    };
                    assertThat(eventTypesExtractor.extractTypes(entity)).containsExactly(Event.class, TestEvent.class);
                },
                () -> {
                    var entity = new EventSourceEntity() {
                        public void apply(Event event, RecordDetails details) {}

                        public void apply(TestEvent event) {}
                    };
                    assertThat(eventTypesExtractor.extractTypes(entity)).containsExactly(TestEvent.class, Event.class);
                },
                () -> {
                    var entity = new EventSourceEntity() {};
                    assertThat(eventTypesExtractor.extractTypes(entity)).isEmpty();
                });
    }
}
