package me.xingzhou.simple.event.store.enrich;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import java.time.Instant;
import java.util.stream.Collectors;
import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.RecordDetails;
import me.xingzhou.simple.event.store.entities.EventSourceEntity;
import org.approvaltests.Approvals;
import org.approvaltests.core.Options;
import org.approvaltests.reporters.ClipboardReporter;
import org.approvaltests.reporters.DiffReporter;
import org.approvaltests.reporters.UseReporter;
import org.approvaltests.scrubbers.DateScrubber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@UseReporter({DiffReporter.class, ClipboardReporter.class})
public class EntityEventApplierTest {

    private static final TestLogger logger = TestLoggerFactory.getTestLogger(EntityEventApplier.class);

    private static String loggedEvents() {
        return logger.getAllLoggingEvents().stream()
                .map(LoggingEvent::getFormattedMessage)
                .collect(Collectors.joining("\n"));
    }

    private final Options approvalOptions =
            new Options().withScrubber(DateScrubber.getScrubberFor("2025-04-20T04:21:48.191772Z"));

    @Test
    @DisplayName("apply(BaseHierarchyEvent)")
    void applyBaseHierarchyEvent() {
        var event = new HierarchyEventWithInterfaces();
        var record = new EventRecord(event, new RecordDetails("stream-name", 0, 0, Instant.now()));
        var subject = new EntityEventApplier();
        var recorder = new Recorder() {
            public void apply(BaseHierarchyEvent event) {
                eventTypeApplied = true;
            }

            @Override
            public String toString() {
                return "%s{eventTypeApplied=%s}".formatted("BaseHierarchyEventAppliedRecorder", eventTypeApplied);
            }
        };

        subject.apply(record, recorder);

        assertThat(recorder.eventTypeApplied()).isTrue();

        Approvals.verify(loggedEvents(), approvalOptions);
    }

    @Test
    @DisplayName("apply(Event)")
    void applyEvent() {
        var event = new HierarchyEventWithInterfaces();
        var record = new EventRecord(event, new RecordDetails("stream-name", 0, 0, Instant.now()));
        var subject = new EntityEventApplier();

        var recorder = new Recorder() {
            public void apply(Event event) {
                eventTypeApplied = true;
            }

            @Override
            public String toString() {
                return "%s{eventTypeApplied=%s}".formatted("EventAppliedRecorder", eventTypeApplied);
            }
        };

        subject.apply(record, recorder);

        assertThat(recorder.eventTypeApplied()).isTrue();

        Approvals.verify(loggedEvents(), approvalOptions);
    }

    @Test
    void applyEventToAnEntityThatDoesNotHaveTheAssociatedApplyMethod() {
        var event = new BaseHierarchyEvent();
        var recorder = new Recorder() {
            @Override
            public String toString() {
                return "%s{eventTypeApplied=%s}".formatted("Anonymous Recorder", eventTypeApplied);
            }
        };
        var subject = new EntityEventApplier();
        assertDoesNotThrow(() -> subject.apply(new EventRecord(event, new RecordDetails("", 0, 0, null)), recorder));
    }

    @Test
    @DisplayName("apply(Event) from super class")
    void applyFromSuperClass() {
        var event = new HierarchyEventWithInterfaces();
        var record = new EventRecord(event, new RecordDetails("stream-name", 0, 0, Instant.now()));
        var subject = new EntityEventApplier();
        var recorder = new SubRecorder();

        subject.apply(record, recorder);

        assertThat(recorder.eventTypeApplied()).isTrue();
        Approvals.verify(loggedEvents(), approvalOptions);
    }

    @Test
    @DisplayName("apply(HierarchyEventWithInterfaces)")
    void applyHierarchyEventWithInterfaces() {
        var event = new HierarchyEventWithInterfaces();
        var record = new EventRecord(event, new RecordDetails("stream-name", 0, 0, Instant.now()));
        var subject = new EntityEventApplier();
        var recorder = new Recorder() {
            public void apply(HierarchyEventWithInterfaces event) {
                eventTypeApplied = true;
            }

            @Override
            public String toString() {
                return "%s{eventTypeApplied=%s}"
                        .formatted("HierarchyEventWithInterfacesEventAppliedRecorder", eventTypeApplied);
            }
        };

        subject.apply(record, recorder);

        assertThat(recorder.eventTypeApplied()).isTrue();

        Approvals.verify(loggedEvents(), approvalOptions);
    }

    @Test
    @DisplayName("apply(TestEventInterfaceA)")
    void applyTestEventInterfaceA() {
        var event = new HierarchyEventWithInterfaces();
        var record = new EventRecord(event, new RecordDetails("stream-name", 0, 0, Instant.now()));
        var subject = new EntityEventApplier();
        var recorder = new Recorder() {
            public void apply(TestEventInterfaceA event) {
                eventTypeApplied = true;
            }

            @Override
            public String toString() {
                return "%s{eventTypeApplied=%s}".formatted("TestEventInterfaceAEventAppliedRecorder", eventTypeApplied);
            }
        };

        subject.apply(record, recorder);

        assertThat(recorder.eventTypeApplied()).isTrue();

        Approvals.verify(loggedEvents(), approvalOptions);
    }

    @Test
    @DisplayName("apply(TestEventInterfaceB)")
    void applyTestEventInterfaceB() {
        var event = new HierarchyEventWithInterfaces();
        var record = new EventRecord(event, new RecordDetails("stream-name", 0, 0, Instant.now()));
        var subject = new EntityEventApplier();
        var recorder = new Recorder() {
            public void apply(TestEventInterfaceB event) {
                eventTypeApplied = true;
            }

            @Override
            public String toString() {
                return "%s{eventTypeApplied=%s}".formatted("TestEventInterfaceBEventAppliedRecorder", eventTypeApplied);
            }
        };

        subject.apply(record, recorder);

        assertThat(recorder.eventTypeApplied()).isTrue();

        Approvals.verify(loggedEvents(), approvalOptions);
    }

    @Test
    @DisplayName("apply(<? extends Event>, RecordDetails)")
    void applyWithRecordDetails() {
        var event = new HierarchyEventWithInterfaces();
        var record = new EventRecord(event, new RecordDetails("stream-name", 0, 0, Instant.now()));
        var subject = new EntityEventApplier();
        var recorder = new EventWithRecordDetailsRecorder();

        subject.apply(record, recorder);

        assertThat(recorder.eventTypeApplied()).isTrue();
        assertThat(recorder.recordedDetails()).isEqualTo(record.details());
        Approvals.verify(loggedEvents(), approvalOptions);
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

    public interface TestEventInterfaceB extends Event {}

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
