package me.xingzhou.simple.event.store.enrich;

import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.RecordDetails;
import me.xingzhou.simple.event.store.entities.EventSourceEntity;
import me.xingzhou.simple.event.store.events.TestEvent;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class EventTypeExtractorTest {

    @Test
    void extract() {
        EventTypeExtractor subject = new EventTypeExtractor();
        assertAll(
                "Extract all relevant events from apply methods",
                () -> {
                    var entity = new EventSourceEntityForEventTypeExtraction() {
                        public void apply(Event event) {
                        }
                    };
                    assertThat(subject.extractTypes(entity)).containsOnly(Event.class);
                },
                () -> {
                    var entity = new EventSourceEntityForEventTypeExtraction() {
                        public void apply(Event event) {
                        }

                        public void apply(TestEvent event) {
                        }
                    };
                    assertThat(subject.extractTypes(entity)).containsExactly(Event.class, TestEvent.class);
                },
                () -> {
                    var entity = new EventSourceEntityForEventTypeExtraction() {
                        public void apply(Event event, RecordDetails details) {
                        }

                        public void apply(TestEvent event) {
                        }
                    };
                    assertThat(subject.extractTypes(entity)).containsExactly(Event.class, TestEvent.class);
                },
                () -> {
                    var entity = new EventSourceEntityForEventTypeExtraction() {
                        /**
                         * spotless:off
                         * Turning spotless off so that the apply methods are not automatically sorted.
                         */
                        public void apply(TestEvent event) {
                        }

                        public void apply(Event event, RecordDetails details) {
                        }

                        /*
                          spotless:on -- leave a space above this toggle
                          so that Eclipse formatter doesn't try to remove it or move it around
                          and break spotless.
                         */
                    };
                    assertThat(subject.extractTypes(entity)).containsExactly(TestEvent.class, Event.class);
                },
                () -> {
                    var entity = new EventSourceEntityForEventTypeExtraction() {
                        public void apply(TestEvent event) {
                        }

                        public void apply(TestEvent event, RecordDetails details) {
                        }
                    };
                    assertThat(subject.extractTypes(entity)).containsExactly(TestEvent.class);
                },
                () -> {
                    var entity = new EventSourceEntityForEventTypeExtraction() {
                    };
                    assertThat(subject.extractTypes(entity)).isEmpty();
                });
    }

    private abstract static class EventSourceEntityForEventTypeExtraction implements EventSourceEntity {
        @Override
        public void handleEnrichedSuccessfully() {

        }

        @Override
        public boolean isEnriched() {
            return false;
        }
    }
}
