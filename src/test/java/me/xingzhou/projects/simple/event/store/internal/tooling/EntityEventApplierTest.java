package me.xingzhou.projects.simple.event.store.internal.tooling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import me.xingzhou.projects.simple.event.store.Event;
import me.xingzhou.projects.simple.event.store.EventRecord;
import org.junit.jupiter.api.Test;

public class EntityEventApplierTest {

    @Test
    void applyEvent() {
        var event = new HierarchyEventWithInterfaces();
        var record = new EventRecord(event, null);
        assertAll(
                "apply hierarchical event to different recorders",
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new EventAppliedRecorder();
                            EntityEventApplier.apply(record, recorder);
                            var eventTypeName = Event.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "Event"),
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new BaseHierarchyEventAppliedRecorder();
                            EntityEventApplier.apply(record, recorder);
                            var eventTypeName = BaseHierarchyEvent.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "BaseHierarchyEvent"),
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new TestEventInterfaceAEventAppliedRecorder();
                            EntityEventApplier.apply(record, recorder);
                            var eventTypeName = TestEventInterfaceA.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "TestEventInterfaceA"),
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new TestEventInterfaceBEventAppliedRecorder();
                            EntityEventApplier.apply(record, recorder);
                            var eventTypeName = TestEventInterfaceB.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "TestEventInterfaceB"),
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new HierarchyEventWithInterfacesEventAppliedRecorder();
                            EntityEventApplier.apply(record, recorder);
                            var eventTypeName = HierarchyEventWithInterfaces.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "HierarchyEventWithInterfaces"),
                () -> assertDoesNotThrow(
                        () -> {
                            var recorder = new SubRecorder();
                            EntityEventApplier.apply(record, recorder);
                            var eventTypeName = Event.class.getSimpleName();
                            assertThat(recorder.eventTypeApplied())
                                    .as(eventTypeName)
                                    .isTrue();
                        },
                        "SubRecorder"));
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

    private abstract static class Recorder {
        protected boolean eventTypeApplied = false;

        public boolean eventTypeApplied() {
            return eventTypeApplied;
        }
    }
}
