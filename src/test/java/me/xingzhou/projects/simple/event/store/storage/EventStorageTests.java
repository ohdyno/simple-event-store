package me.xingzhou.projects.simple.event.store.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.Instant;
import java.util.*;
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
            var eventId = "an-event-id";
            var eventType = "an-event-type";
            var eventContent = """
                    {"key", "value"}""";

            var record = storage.createStream(streamName, eventId, eventType, eventContent);

            assertThat(record)
                    .isEqualTo(new StoredRecord(
                            streamName,
                            eventId,
                            eventType,
                            eventContent,
                            EventStorage.VersionConstants.NEW_STREAM,
                            storage.lastUpdateAt()));
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
                        "first-event-id", EVENT_TYPE_A, """
                                {"key1", "value"}"""),
                new RequestEvent(
                        "second-event-id",
                        EVENT_TYPE_B, // must be unique from the other event type
                        """
                                {"key2", "value"}"""),
                new RequestEvent(
                        "third-event-id", EVENT_TYPE_A, """
                                {"key3", "value"}"""));

        private List<StoredRecord> storedRecords;

        @BeforeEach
        void seedEvents() {
            this.storedRecords = save(this.events, streamName);
        }

        @Test
        @DisplayName("Retrieve events from the stream successfully.")
        void retrieveEvents() {
            var expected = storedRecords;

            var records = storage.retrieveEvents(
                    streamName,
                    Collections.emptyList(),
                    EventStorage.VersionConstants.RANGE_MIN_EXCLUSIVE,
                    EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::version));
            assertThat(records.version()).isEqualTo(storedRecords.getLast().version());
        }

        @Test
        @DisplayName("Retrieve specific one event type from the stream successfully.")
        void retrieveSpecificEventType() {
            var eventTypes = List.of(EVENT_TYPE_A);
            var expected = List.of(storedRecords.get(0), storedRecords.get(2));

            var records = storage.retrieveEvents(
                    streamName,
                    eventTypes,
                    EventStorage.VersionConstants.RANGE_MIN_EXCLUSIVE,
                    EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::version));
            assertThat(records.version()).isEqualTo(storedRecords.getLast().version());
        }

        @Test
        @DisplayName("Retrieve specific multiple event types from the stream successfully.")
        void retrieveSpecificMultipleEventTypes() {
            var eventTypes = List.of(EVENT_TYPE_A, EVENT_TYPE_B);
            var expected = storedRecords;

            var records = storage.retrieveEvents(
                    streamName,
                    eventTypes,
                    EventStorage.VersionConstants.RANGE_MIN_EXCLUSIVE,
                    EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::version));
            assertThat(records.version()).isEqualTo(storedRecords.getLast().version());
        }

        @Test
        @DisplayName("Retrieve events from the stream after a specific version successfully.")
        void retrieveEventsAfterVersion() {
            var expected = List.of(storedRecords.get(1), storedRecords.get(2));

            var records = storage.retrieveEvents(
                    streamName,
                    Collections.emptyList(),
                    storedRecords.getFirst().version(),
                    EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::version));
            assertThat(records.version()).isEqualTo(storedRecords.getLast().version());
        }

        @Test
        @DisplayName("Retrieve events from the stream up to a specific version successfully.")
        void retrieveEventsUpToVersion() {
            var expected = List.of(storedRecords.get(0), storedRecords.get(1));

            var records = storage.retrieveEvents(
                    streamName,
                    Collections.emptyList(),
                    EventStorage.VersionConstants.RANGE_MIN_EXCLUSIVE,
                    storedRecords.get(1).version());

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::version));
            assertThat(records.version()).isEqualTo(storedRecords.getLast().version());
        }

        @Test
        @DisplayName(
                "Retrieve events from the stream after a specific version and up to a specific version successfully.")
        void retrieveEventsAfterSpecificVersionAndUpToVersion() {
            var expected = List.of(storedRecords.get(1));

            var records = storage.retrieveEvents(
                    streamName,
                    Collections.emptyList(),
                    storedRecords.getFirst().version(),
                    storedRecords.get(1).version());

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::version));
            assertThat(records.version()).isEqualTo(storedRecords.getLast().version());
        }

        @Test
        @DisplayName("Retrieve events from the stream with an event type and range filter successfully.")
        void retrieveEventsWithEventTypeAndRangeFilter() {
            var expected = List.of(storedRecords.get(1));

            var records = storage.retrieveEvents(
                    streamName,
                    List.of(EVENT_TYPE_B),
                    storedRecords.getFirst().version(),
                    EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::version));
            assertThat(records.version()).isEqualTo(storedRecords.getLast().version());
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
                                storedRecords.getLast().version(),
                                EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

                        assertThat(record.records()).isEmpty();
                        assertThat(record.version())
                                .isEqualTo(storedRecords.getLast().version());
                    },
                    () -> {
                        // end > begin
                        var record = storage.retrieveEvents(
                                streamName,
                                Collections.emptyList(),
                                storedRecords.getLast().version(),
                                storedRecords.getFirst().version());

                        assertThat(record.records()).isEmpty();
                        assertThat(record.version())
                                .isEqualTo(storedRecords.getLast().version());
                    },
                    () -> {
                        // end = begin
                        var record = storage.retrieveEvents(
                                streamName,
                                Collections.emptyList(),
                                storedRecords.getFirst().version(),
                                storedRecords.getFirst().version());

                        assertThat(record.records()).isEmpty();
                        assertThat(record.version())
                                .isEqualTo(storedRecords.getLast().version());
                    },
                    () -> {
                        // max version
                        var record = storage.retrieveEvents(
                                streamName,
                                Collections.emptyList(),
                                EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE,
                                EventStorage.VersionConstants.RANGE_MAX_INCLUSIVE);

                        assertThat(record.records()).isEmpty();
                        assertThat(record.version())
                                .isEqualTo(storedRecords.getLast().version());
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
        private static final String STREAM_ONE = "stream-one";
        private static final String STREAM_TWO = "stream-two";
        private static final String EVENT_TYPE_A = "event-type-a";
        private static final String EVENT_TYPE_B = "event-type-b";
        private final Map<String, List<RequestEvent>> streams = Map.of(
                STREAM_ONE,
                List.of(
                        new RequestEvent(
                                "first-event-id",
                                EVENT_TYPE_A,
                                """
                                        {"key1", "in-stream-one"}"""),
                        new RequestEvent(
                                "second-event-id",
                                EVENT_TYPE_B, // must be unique from the other event type
                                """
                                        {"key2", "in-stream-one"}"""),
                        new RequestEvent(
                                "third-event-id",
                                EVENT_TYPE_A,
                                """
                                        {"key3", "in-stream-one"}""")),
                STREAM_TWO,
                List.of(
                        new RequestEvent(
                                "first-event-id",
                                EVENT_TYPE_A,
                                """
                                        {"key1", "in-stream-two"}"""),
                        new RequestEvent(
                                "second-event-id",
                                EVENT_TYPE_B, // must be unique from the other event type
                                """
                                        {"key2", "in-stream-two"}"""),
                        new RequestEvent(
                                "third-event-id",
                                EVENT_TYPE_A,
                                """
                                        {"key3", "in-stream-two"}""")));

        private List<StoredRecord> storedRecords;

        @BeforeEach
        void seedEvents() {
            storedRecords = streams.entrySet().stream()
                    .flatMap(entry -> save(entry.getValue(), entry.getKey()).stream())
                    .toList();
        }

        @Test
        @DisplayName("Retrieve events from all streams and all types")
        void retrieveEventsFromAllStreamsAndTypes() {
            var expected = storedRecords;

            var records =
                    storage.retrieveEvents(Instant.MIN, Instant.MAX, Collections.emptyList(), Collections.emptyList());

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::timestamp));
            assertThat(records.timestamp()).isEqualTo(storage.lastUpdateAt());
        }

        @Test
        @DisplayName("Retrieve events from one stream and all types")
        void retrieveEventsFromOneStreamAndTypes() {
            var streamNames = List.of(STREAM_ONE);
            var expected = storedRecords.stream()
                    .filter(record -> streamNames.contains(record.streamName()))
                    .toList();

            var records = storage.retrieveEvents(Instant.MIN, Instant.MAX, streamNames, Collections.emptyList());

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::timestamp));
            assertThat(records.timestamp()).isEqualTo(storage.lastUpdateAt());
        }

        @Test
        @DisplayName("Retrieve events from multiple streams and all types")
        void retrieveEventsFromMultipleStreamsAndTypes() {
            var streamNames = List.of(STREAM_ONE, STREAM_TWO);
            var expected = storedRecords;

            var records = storage.retrieveEvents(Instant.MIN, Instant.MAX, streamNames, Collections.emptyList());

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::timestamp));
            assertThat(records.timestamp()).isEqualTo(storage.lastUpdateAt());
        }

        @Test
        @DisplayName("Retrieve events from all streams and one event type")
        void retrieveEventsFromAllStreamsAndOneEventType() {
            var eventTypes = List.of(EVENT_TYPE_A);
            var expected = storedRecords.stream()
                    .filter(record -> eventTypes.contains(record.eventType()))
                    .toList();

            var records = storage.retrieveEvents(Instant.MIN, Instant.MAX, Collections.emptyList(), eventTypes);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::timestamp));
            assertThat(records.timestamp()).isEqualTo(storage.lastUpdateAt());
        }

        @Test
        @DisplayName("Retrieve events from all streams and multiple event types")
        void retrieveEventsFromAllStreamsAndMultipleEventTypes() {
            var eventTypes = List.of(EVENT_TYPE_A, EVENT_TYPE_B);
            var expected = storedRecords;

            var records = storage.retrieveEvents(Instant.MIN, Instant.MAX, Collections.emptyList(), eventTypes);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::timestamp));
            assertThat(records.timestamp()).isEqualTo(storage.lastUpdateAt());
        }

        @Test
        @DisplayName("Retrieve events from some streams and some event types")
        void retrieveEventsFromSomeStreamsAndSomeEventTypes() {
            var eventTypes = List.of(EVENT_TYPE_A);
            var streamNames = List.of(STREAM_ONE);
            var expected = storedRecords.stream()
                    .filter(record ->
                            eventTypes.contains(record.eventType()) && streamNames.contains(record.streamName()))
                    .toList();

            var records = storage.retrieveEvents(Instant.MIN, Instant.MAX, streamNames, eventTypes);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::timestamp));
            assertThat(records.timestamp()).isEqualTo(storage.lastUpdateAt());
        }
    }

    private List<StoredRecord> save(List<RequestEvent> events, String streamName) {
        var records = new ArrayList<StoredRecord>();
        var first = events.get(0);
        var record = storage.createStream(streamName, first.eventId(), first.eventType(), first.eventContent());
        records.add(record);

        for (var event : events.stream().skip(1).toList()) {
            var newRecord = storage.appendEvent(
                    streamName, record.version(), event.eventId(), event.eventType(), event.eventContent());
            records.add(newRecord);
            record = newRecord;
        }
        return records;
    }

    private static final class RequestEvent {
        private final String eventId;
        private final String eventType;
        private final String eventContent;

        private long version;
        private Instant timestamp;

        private RequestEvent(String eventId, String eventType, String eventContent, long version, Instant timestamp) {
            this.eventId = eventId;
            this.eventType = eventType;
            this.eventContent = eventContent;
            this.version = version;
            this.timestamp = timestamp;
        }

        public RequestEvent(String eventId, String eventType, String eventContent) {
            this(
                    eventId,
                    eventType,
                    eventContent,
                    EventStorage.VersionConstants.UNDEFINED_STREAM,
                    EventStorage.TimestampConstants.NEVER);
        }

        private static RequestEvent fromStoredRecord(StoredRecord record) {
            return new RequestEvent(
                    record.eventId(), record.eventType(), record.eventContent(), record.version(), record.timestamp());
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
