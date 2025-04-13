package me.xingzhou.simple.event.store.enrich;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.Instant;
import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.RecordDetails;
import me.xingzhou.simple.event.store.entities.EventSourceEntity;
import me.xingzhou.simple.event.store.events.TestEventTypeConverter;
import org.junit.jupiter.api.Test;

public class EntityEventApplierTest {

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
    }

    public static class BaseHierarchyEvent implements Event {}

    public static class BaseHierarchyEventAppliedRecorder extends Recorder {
        public void apply(BaseHierarchyEvent event) {
            eventTypeApplied = true;
        }
    }

    public static class EventAppliedRecorder extends Recorder {
        public void apply(Event event) {
            eventTypeApplied = true;
        }
    }

    public static class EventWithRecordDetailsRecorder extends Recorder {
        private RecordDetails recordedDetails = null;

        public void apply(Event event, RecordDetails recordDetails) {
            eventTypeApplied = true;
            recordedDetails = recordDetails;
        }

        private RecordDetails recordedDetails() {
            return recordedDetails;
        }
    }

    public static class HierarchyEventWithInterfaces extends BaseHierarchyEvent
            implements TestEventInterfaceA, TestEventInterfaceB {}

    public static class HierarchyEventWithInterfacesEventAppliedRecorder extends Recorder {
        public void apply(HierarchyEventWithInterfaces event) {
            eventTypeApplied = true;
        }
    }

    public static class SubRecorder extends SuperRecorder {}

    public static class SuperRecorder extends Recorder {
        public void apply(Event event) {
            eventTypeApplied = true;
        }
    }

    public interface TestEventInterfaceA extends Event {}

    public static class TestEventInterfaceAEventAppliedRecorder extends Recorder {
        public void apply(TestEventInterfaceA event) {
            eventTypeApplied = true;
        }
    }

    public interface TestEventInterfaceB extends Event {}

    public static class TestEventInterfaceBEventAppliedRecorder extends Recorder {
        public void apply(TestEventInterfaceB event) {
            eventTypeApplied = true;
        }
    }

    private abstract static class Recorder implements EventSourceEntity {
        protected boolean eventTypeApplied = false;

        public boolean eventTypeApplied() {
            return eventTypeApplied;
        }
    }
}
