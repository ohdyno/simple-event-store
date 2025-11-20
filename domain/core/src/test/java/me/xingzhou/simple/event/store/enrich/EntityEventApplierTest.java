package me.xingzhou.simple.event.store.enrich;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import java.time.Instant;
import java.util.stream.Collectors;
import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.RecordDetails;
import me.xingzhou.simple.event.store.entities.EventSourceEntity;
import me.xingzhou.simple.event.store.events.TestEventTypeConverter;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.scrubbers.DateScrubber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EntityEventApplierTest {

    private static final TestLogger logger = TestLoggerFactory.getTestLogger(EntityEventApplier.class);

    @Test
    void applyEvent() {
        var event = new HierarchyEventWithInterfaces();
        var record = new EventRecord(event, new RecordDetails("stream-name", 0, 0, Instant.now()));
        var subject = new EntityEventApplier(new EventTypesExtractor(new TestEventTypeConverter()));

        assertAll(
                "apply hierarchical event to different recorders",
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new EventAppliedRecorder();
                            subject.apply(record, recorder);
                            var eventTypeName = Event.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "apply(Event)"),
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new BaseHierarchyEventAppliedRecorder();
                            subject.apply(record, recorder);
                            var eventTypeName = BaseHierarchyEvent.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "apply(BaseHierarchyEvent)"),
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new TestEventInterfaceAEventAppliedRecorder();
                            subject.apply(record, recorder);
                            var eventTypeName = TestEventInterfaceA.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "apply(TestEventInterfaceA)"),
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new TestEventInterfaceBEventAppliedRecorder();
                            subject.apply(record, recorder);
                            var eventTypeName = TestEventInterfaceB.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "apply(TestEventInterfaceB)"),
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new HierarchyEventWithInterfacesEventAppliedRecorder();
                            subject.apply(record, recorder);
                            var eventTypeName = HierarchyEventWithInterfaces.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "apply(HierarchyEventWithInterfaces)"),
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new SubRecorder();
                            subject.apply(record, recorder);
                            var eventTypeName = Event.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "apply(Event) from super class"),
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new EventWithRecordDetailsRecorder();
                            subject.apply(record, recorder);
                            var eventTypeName = Event.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                            assertThat(recorder.recordedDetails()).isEqualTo(record.details());
                        },
                        "apply(<? extends Event>, RecordDetails)"));

        var loggedEvents = logger.getAllLoggingEvents().stream()
                .map(LoggingEvent::toString)
                .collect(Collectors.joining("\n"));
        Approvals.verify(
                loggedEvents, new Options().withScrubber(DateScrubber.getScrubberFor("2025-04-20T04:21:48.191772Z")));
    }

    @BeforeEach
    void setUp() {
        logger.clearAll();
    }

    public static class BaseHierarchyEvent implements Event {
        @Override
        public String toString() {
            return "BaseHierarchyEvent{}";
        }
    }

    public static class BaseHierarchyEventAppliedRecorder extends Recorder {
        public void apply(BaseHierarchyEvent event) {
            eventTypeApplied = true;
        }

        @Override
        public String toString() {
            return "BaseHierarchyEventAppliedRecorder{" + "eventTypeApplied=" + eventTypeApplied + '}';
        }
    }

    public static class EventAppliedRecorder extends Recorder {
        public void apply(Event event) {
            eventTypeApplied = true;
        }

        @Override
        public String toString() {
            return "EventAppliedRecorder{}";
        }
    }

    public static class EventWithRecordDetailsRecorder extends Recorder {
        private RecordDetails recordedDetails = null;

        public void apply(Event event, RecordDetails recordDetails) {
            eventTypeApplied = true;
            recordedDetails = recordDetails;
        }

        @Override
        public String toString() {
            return "EventWithRecordDetailsRecorder{" + "recordedDetails=" + recordedDetails + ", eventTypeApplied="
                    + eventTypeApplied + '}';
        }

        private RecordDetails recordedDetails() {
            return recordedDetails;
        }
    }

    public static class HierarchyEventWithInterfaces extends BaseHierarchyEvent
            implements TestEventInterfaceA, TestEventInterfaceB {
        @Override
        public String toString() {
            return "HierarchyEventWithInterfaces{}";
        }
    }

    public static class HierarchyEventWithInterfacesEventAppliedRecorder extends Recorder {
        public void apply(HierarchyEventWithInterfaces event) {
            eventTypeApplied = true;
        }

        @Override
        public String toString() {
            return "HierarchyEventWithInterfacesEventAppliedRecorder{" + "eventTypeApplied=" + eventTypeApplied + '}';
        }
    }

    public static class SubRecorder extends SuperRecorder {
        @Override
        public String toString() {
            return "SubRecorder{" + "eventTypeApplied=" + eventTypeApplied + '}';
        }
    }

    public static class SuperRecorder extends Recorder {
        public void apply(Event event) {
            eventTypeApplied = true;
        }

        @Override
        public String toString() {
            return "SuperRecorder{" + "eventTypeApplied=" + eventTypeApplied + '}';
        }
    }

    public interface TestEventInterfaceA extends Event {}

    public static class TestEventInterfaceAEventAppliedRecorder extends Recorder {
        public void apply(TestEventInterfaceA event) {
            eventTypeApplied = true;
        }

        @Override
        public String toString() {
            return "TestEventInterfaceAEventAppliedRecorder{" + "eventTypeApplied=" + eventTypeApplied + '}';
        }
    }

    public interface TestEventInterfaceB extends Event {}

    public static class TestEventInterfaceBEventAppliedRecorder extends Recorder {
        public void apply(TestEventInterfaceB event) {
            eventTypeApplied = true;
        }

        @Override
        public String toString() {
            return "TestEventInterfaceBEventAppliedRecorder{" + "eventTypeApplied=" + eventTypeApplied + '}';
        }
    }

    private abstract static class Recorder implements EventSourceEntity {
        protected boolean eventTypeApplied = false;

        public boolean eventTypeApplied() {
            return eventTypeApplied;
        }

        @Override
        public void handleEnrichedSuccessfully() {}

        @Override
        public boolean isEnriched() {
            return false;
        }

        @Override
        public String toString() {
            return "Recorder{" + "eventTypeApplied=" + eventTypeApplied + '}';
        }
    }
}
