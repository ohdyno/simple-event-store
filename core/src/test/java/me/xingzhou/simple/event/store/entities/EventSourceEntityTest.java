package me.xingzhou.simple.event.store.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.event.converter.ServiceLoaderEventTypeConverter;
import me.xingzhou.simple.event.store.internal.tooling.EventTypesExtractor;
import me.xingzhou.simple.event.store.serializer.events.FooEvent;
import org.junit.jupiter.api.Test;

class EventSourceEntityTest {

    private final EventTypesExtractor eventTypesExtractor =
            new EventTypesExtractor(new ServiceLoaderEventTypeConverter());

    @Test
    void extractEventTypesFromApplyMethods() {
        assertAll(
                "Extract all relevant events from apply methods",
                () -> {
                    var entity = new EventSourceEntity() {
                        public void apply(Event event) {}
                    };
                    assertThat(eventTypesExtractor.extractTypes(entity)).containsExactlyInAnyOrder(Event.class);
                },
                () -> {
                    var entity = new EventSourceEntity() {
                        public void apply(Event event) {}

                        public void apply(FooEvent event) {}
                    };
                    assertThat(eventTypesExtractor.extractTypes(entity))
                            .containsExactlyInAnyOrder(Event.class, FooEvent.class);
                });
    }
}
