package me.xingzhou.projects.simple.event.store.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import me.xingzhou.projects.simple.event.store.Event;
import me.xingzhou.projects.simple.event.store.serializer.events.FooEvent;
import org.junit.jupiter.api.Test;

class EventSourceEntityTest {

    private EventSourceEntity subject = new EventSourceEntity() {};

    @Test
    void extractEventTypesFromApplyMethods() {
        assertAll(
                "Extract all relevant events from apply methods",
                () -> {
                    var subject = new EventSourceEntity() {
                        public void apply(Event event) {}
                    };
                    assertThat(subject.extractEventTypesFromApplyMethods()).containsExactlyInAnyOrder(Event.class);
                },
                () -> {
                    var subject = new EventSourceEntity() {
                        public void apply(Event event) {}

                        public void apply(FooEvent event) {}
                    };
                    assertThat(subject.extractEventTypesFromApplyMethods())
                            .containsExactlyInAnyOrder(Event.class, FooEvent.class);
                });
    }

    @Test
    void isApplyMethod() {}
}
