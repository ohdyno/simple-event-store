package me.xingzhou.projects.simple.event.store.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.NoSuchStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.StaleVersionFailure;
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

            assertThat(version).isEqualTo(EventStorage.VersionConstants.NEW_STREAM);
        }

        @Test
        @DisplayName("Create a duplicate stream fails.")
        void createDuplicateStream() {
            storage.createStream(
                    streamName, "an-event-id", "an-event-type", """
                            {"key", "value"}""");

            assertThatThrownBy(() -> storage.createStream(streamName, "anything", "anything", "anything"))
                    .isInstanceOf(DuplicateEventStreamFailure.class);
        }

        @Test
        @DisplayName("Retrieve the event from a stream that does not exist fails.")
        void retrieveEvents() {
            assertThatThrownBy(() -> storage.retrieveEvents(
                            streamName,
                            Collections.emptyList(),
                            EventStorage.VersionConstants.RANGE_MIN_EXCLUSIVE,
                            EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE))
                    .isInstanceOf(NoSuchStreamFailure.class);
        }

        @Test
        @DisplayName("Append to a stream that does not exist fails.")
        void appendToStreamThatDoesNotExist() {
            assertThatThrownBy(() -> storage.appendEvent(
                            "a-stream-that-does-not-exist",
                            EventStorage.VersionConstants.NEW_STREAM,
                            "anything",
                            "anything",
                            "anything"))
                    .isInstanceOf(NoSuchStreamFailure.class);
        }
    }

    @Nested
    class OneStreamStorageTests {
        private static final String EVENT_TYPE_A = "event-type-a";
        private static final String EVENT_TYPE_B = "event-type-b";
        private final String streamName = "a-stream-name";
        private final List<RequestEvent> events = List.of(
                new RequestEvent(
                        "first-event-id",
                        EVENT_TYPE_A,
                        """
                            {"key1", "value"}""",
                        EventStorage.VersionConstants.UNDEFINED_STREAM),
                new RequestEvent(
                        "second-event-id",
                        EVENT_TYPE_B, // must be unique from the other event type
                        """
                            {"key2", "value"}""",
                        EventStorage.VersionConstants.UNDEFINED_STREAM),
                new RequestEvent(
                        "third-event-id",
                        EVENT_TYPE_A,
                        """
                            {"key3", "value"}""",
                        EventStorage.VersionConstants.UNDEFINED_STREAM));

        @BeforeEach
        void seedEvents() {
            save(this.events, streamName);
        }

        @Test
        @DisplayName("Retrieve events from the stream successfully.")
        void retrieveEvents() {
            var expected = events.stream()
                    .map(event ->
                            new StoredRecord(event.eventType(), event.eventContent(), streamName, event.version()))
                    .toList();

            var records = storage.retrieveEvents(
                    streamName,
                    Collections.emptyList(),
                    EventStorage.VersionConstants.RANGE_MIN_EXCLUSIVE,
                    EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

            assertThat(records.records()).containsExactlyElementsOf(expected);
            assertThat(records.version()).isEqualTo(events.getLast().version());
        }

        @Test
        @DisplayName("Retrieve specific one event type from the stream successfully.")
        void retrieveSpecificEventType() {
            var eventTypes = List.of(EVENT_TYPE_A);
            var expected = events.stream()
                    .filter(event -> eventTypes.contains(event.eventType()))
                    .map(event ->
                            new StoredRecord(event.eventType(), event.eventContent(), streamName, event.version()))
                    .toList();

            var records = storage.retrieveEvents(
                    streamName,
                    eventTypes,
                    EventStorage.VersionConstants.RANGE_MIN_EXCLUSIVE,
                    EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

            assertThat(records.records()).containsExactlyElementsOf(expected);
            assertThat(records.version()).isEqualTo(events.getLast().version());
        }

        @Test
        @DisplayName("Retrieve specific multiple event types from the stream successfully.")
        void retrieveSpecificMultipleEventTypes() {
            var eventTypes = List.of(EVENT_TYPE_A, EVENT_TYPE_B);
            var expected = events.stream()
                    .filter(event -> eventTypes.contains(event.eventType()))
                    .map(event ->
                            new StoredRecord(event.eventType(), event.eventContent(), streamName, event.version()))
                    .toList();

            var records = storage.retrieveEvents(
                    streamName,
                    eventTypes,
                    EventStorage.VersionConstants.RANGE_MIN_EXCLUSIVE,
                    EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

            assertThat(records.records()).containsExactlyElementsOf(expected);
            assertThat(records.version()).isEqualTo(events.getLast().version());
        }

        @Test
        @DisplayName("Retrieve events from the stream after a specific version successfully.")
        void retrieveEventsAfterVersion() {
            var expected = Stream.of(events.get(1), events.get(2))
                    .map(event ->
                            new StoredRecord(event.eventType(), event.eventContent(), streamName, event.version()))
                    .toList();

            var records = storage.retrieveEvents(
                    streamName,
                    Collections.emptyList(),
                    events.getFirst().version(),
                    EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

            assertThat(records.records()).containsExactlyElementsOf(expected);
            assertThat(records.version()).isEqualTo(events.getLast().version());
        }

        @Test
        @DisplayName("Retrieve events from the stream up to a specific version successfully.")
        void retrieveEventsUpToVersion() {
            var expected = Stream.of(events.get(0), events.get(1))
                    .map(event ->
                            new StoredRecord(event.eventType(), event.eventContent(), streamName, event.version()))
                    .toList();

            var records = storage.retrieveEvents(
                    streamName,
                    Collections.emptyList(),
                    EventStorage.VersionConstants.RANGE_MIN_EXCLUSIVE,
                    events.get(1).version());

            assertThat(records.records()).containsExactlyElementsOf(expected);
            assertThat(records.version()).isEqualTo(events.getLast().version());
        }

        @Test
        @DisplayName(
                "Retrieve events from the stream after a specific version and up to a specific version successfully.")
        void retrieveEventsAfterSpecificVersionAndUpToVersion() {
            var expected = Stream.of(events.get(1))
                    .map(event ->
                            new StoredRecord(event.eventType(), event.eventContent(), streamName, event.version()))
                    .toList();

            var records = storage.retrieveEvents(
                    streamName,
                    Collections.emptyList(),
                    events.getFirst().version(),
                    events.get(1).version());

            assertThat(records.records()).containsExactlyElementsOf(expected);
            assertThat(records.version()).isEqualTo(events.getLast().version());
        }

        @Test
        @DisplayName("Retrieve events from the stream with an event type and range filter successfully.")
        void retrieveEventsWithEventTypeAndRangeFilter() {
            var expected = Stream.of(events.get(1))
                    .map(event ->
                            new StoredRecord(event.eventType(), event.eventContent(), streamName, event.version()))
                    .toList();

            var records = storage.retrieveEvents(
                    streamName,
                    List.of(EVENT_TYPE_B),
                    events.getFirst().version(),
                    EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

            assertThat(records.records()).containsExactlyElementsOf(expected);
            assertThat(records.version()).isEqualTo(events.getLast().version());
        }

        @Test
        @DisplayName("Retrieve events from the stream with version range edge cases return no events successfully.")
        void retrieveEventsVersionRange() {
            assertAll(
                    "",
                    () -> {
                        // skipping all events
                        var record = storage.retrieveEvents(
                                streamName,
                                Collections.emptyList(),
                                events.getLast().version(),
                                EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

                        assertThat(record.records()).isEmpty();
                        assertThat(record.version()).isEqualTo(events.getLast().version());
                    },
                    () -> {
                        // end > begin
                        var record = storage.retrieveEvents(
                                streamName,
                                Collections.emptyList(),
                                events.getLast().version(),
                                events.getFirst().version());

                        assertThat(record.records()).isEmpty();
                        assertThat(record.version()).isEqualTo(events.getLast().version());
                    },
                    () -> {
                        // end = begin
                        var record = storage.retrieveEvents(
                                streamName,
                                Collections.emptyList(),
                                events.getFirst().version(),
                                events.getFirst().version());

                        assertThat(record.records()).isEmpty();
                        assertThat(record.version()).isEqualTo(events.getLast().version());
                    },
                    () -> {
                        // max version
                        var record = storage.retrieveEvents(
                                streamName,
                                Collections.emptyList(),
                                EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE,
                                EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

                        assertThat(record.records()).isEmpty();
                        assertThat(record.version()).isEqualTo(events.getLast().version());
                    });
        }

        @Test
        @DisplayName("Append to stream with stale version fails.")
        void appendEventWithStaleVersion() {
            assertThatThrownBy(() -> storage.appendEvent(
                            streamName, EventStorage.VersionConstants.NEW_STREAM, "anything", "anything", "anything"))
                    .isInstanceOf(StaleVersionFailure.class);
        }
    }

    @Nested
    class MultipleStreamsStorageTests {
        private final Map<String, List<RequestEvent>> events = Map.of(
                "stream-one",
                        List.of(
                                new RequestEvent(
                                        "first-event-id",
                                        "event-type-a",
                                        """
                        {"key1", "in-stream-one"}""",
                                        EventStorage.VersionConstants.UNDEFINED_STREAM),
                                new RequestEvent(
                                        "second-event-id",
                                        "event-type-b", // must be unique from the other event type
                                        """
                        {"key2", "in-stream-one"}""",
                                        EventStorage.VersionConstants.UNDEFINED_STREAM),
                                new RequestEvent(
                                        "third-event-id",
                                        "event-type-a",
                                        """
                        {"key3", "in-stream-one"}""",
                                        EventStorage.VersionConstants.UNDEFINED_STREAM)),
                "stream-two",
                        List.of(
                                new RequestEvent(
                                        "first-event-id",
                                        "event-type-a",
                                        """
                        {"key1", "in-stream-two"}""",
                                        EventStorage.VersionConstants.UNDEFINED_STREAM),
                                new RequestEvent(
                                        "second-event-id",
                                        "event-type-b", // must be unique from the other event type
                                        """
                        {"key2", "in-stream-two"}""",
                                        EventStorage.VersionConstants.UNDEFINED_STREAM),
                                new RequestEvent(
                                        "third-event-id",
                                        "event-type-a",
                                        """
                        {"key3", "in-stream-two"}""",
                                        EventStorage.VersionConstants.UNDEFINED_STREAM)));

        @BeforeEach
        void seedEvents() {
            events.forEach((streamName, events) -> save(events, streamName));
        }

        @Test
        @DisplayName("Retrieve events from all streams and all types")
        void retrieveEventsFromAllStreamsAndTypes() {
            storage.retrieveEvents(Instant.MIN, Instant.MAX, Collections.emptyList(), Collections.emptyList());
        }
    }

    private void save(List<RequestEvent> events, String streamName) {
        var first = events.get(0);
        var version = storage.createStream(streamName, first.eventId(), first.eventType(), first.eventContent());
        first.setVersion(version);

        for (var event : events.stream().skip(1).toList()) {
            version =
                    storage.appendEvent(streamName, version, event.eventId(), event.eventType(), event.eventContent());
            event.setVersion(version);
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
