package me.xingzhou.projects.simple.event.store.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.NoSuchStreamFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public abstract class EventStorageTests {
    private EventStorage storage;

    protected abstract EventStorage createStorage();

    @BeforeEach
    void setUp() {
        this.storage = createStorage();
    }

    @Nested
    class EmptyStorageTests {
        private final String streamName = "a-stream-name";

        @Test
        @DisplayName("Create a stream successfully")
        void createStream() {
            var version = storage.createStream(
                    streamName, "an-event-id", "an-event-type", """
                            {"key", "value"}""");

            assertThat(version).isEqualTo(storage.newStreamVersion());
        }

        @Test
        @DisplayName("Retrieve the event from a stream that does not exist fails.")
        void retrieveEvents() {
            assertThatThrownBy(() -> storage.retrieveEvents(
                            streamName,
                            Collections.emptyList(),
                            storage.undefinedVersion(),
                            storage.exclusiveMaxVersion()))
                    .isInstanceOf(NoSuchStreamFailure.class);
        }
    }

    @Nested
    class OneStreamStorageTests {
        private final String streamName = "a-stream-name";
        private List<RequestEvent> events;

        @BeforeEach
        void setUp() {
            var events = List.of(
                    new RequestEvent(
                            "first-event-id",
                            "an-event-type",
                            """
                            {"key1", "value"}""",
                            storage.undefinedVersion()),
                    new RequestEvent(
                            "second-event-id",
                            "an-event-type",
                            """
                            {"key2", "value"}""",
                            storage.undefinedVersion()));

            events.forEach(event -> {
                try {
                    var version =
                            storage.createStream(streamName, event.eventId(), event.eventType(), event.eventContent());
                    event.setVersion(version);
                } catch (DuplicateEventStreamFailure ignored) {
                    var version = storage.appendEvent(
                            streamName,
                            storage.newStreamVersion(),
                            event.eventId(),
                            event.eventType(),
                            event.eventContent());
                    event.setVersion(version);
                }
            });

            this.events = events;
        }

        @Test
        @DisplayName("Retrieve the event from the stream.")
        void retrieveEvents() {
            var expected = events.stream()
                    .map(event ->
                            new StoredRecord(event.eventType(), event.eventContent(), streamName, event.version()))
                    .toList();

            var records = storage.retrieveEvents(
                    streamName, Collections.emptyList(), storage.undefinedVersion(), storage.exclusiveMaxVersion());

            assertThat(records.records()).containsExactlyElementsOf(expected);
        }

        @Test
        @DisplayName("Create a duplicate stream fails.")
        void createDuplicateStream() {
            assertThatThrownBy(() -> storage.createStream(streamName, "anything", "anything", "anything"))
                    .isInstanceOf(DuplicateEventStreamFailure.class);
        }
    }

    private static final class RequestEvent {
        private final String eventId;
        private final String eventType;
        private final String eventContent;

        private long version;

        private RequestEvent(String eventId, String eventType, String eventContent, long version) {
            this.eventId = eventId;
            this.eventType = eventType;
            this.eventContent = eventContent;
            this.version = version;
        }

        public String eventId() {
            return eventId;
        }

        public String eventType() {
            return eventType;
        }

        public String eventContent() {
            return eventContent;
        }

        public long version() {
            return version;
        }

        private void setVersion(long version) {
            this.version = version;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (RequestEvent) obj;
            return Objects.equals(this.eventId, that.eventId)
                    && Objects.equals(this.eventType, that.eventType)
                    && Objects.equals(this.eventContent, that.eventContent)
                    && this.version == that.version;
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventId, eventType, eventContent, version);
        }

        @Override
        public String toString() {
            return "RequestEvent[" + "eventId="
                    + eventId + ", " + "eventType="
                    + eventType + ", " + "eventContent="
                    + eventContent + ", " + "version="
                    + version + ']';
        }
    }
}
