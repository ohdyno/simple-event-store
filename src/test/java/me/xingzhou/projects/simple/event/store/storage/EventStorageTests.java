package me.xingzhou.projects.simple.event.store.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.*;
import me.xingzhou.projects.simple.event.store.storage.EventStorage.Constants.Ids;
import me.xingzhou.projects.simple.event.store.storage.EventStorage.Constants.Versions;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.NoSuchStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.StaleVersionFailure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public abstract class EventStorageTests {
    private static List<StoredRecord> flatten(HashMap<String, List<StoredRecord>> storedRecords) {
        return storedRecords.values().stream().flatMap(List::stream).toList();
    }

    private EventStorage storage;

    protected abstract EventStorage createStorage();

    @BeforeEach
    void setUp() {
        this.storage = createStorage();
    }

    private record RequestEvent(String id, String eventType, String eventContent) {}

    /**
     * Store the events to the event stream and record the StoredRecord into storedRecords.
     *
     * @param events are the events to be saved to the stream.
     * @param streamName is the name of the stream. If the stream does not exist, then a stream is created.
     * @param storedRecords are the records that have already been stored in the event stream. New records are added to
     *     the map referred by the parameter. This parameter is also used to find the current version of the stream for
     *     the append operation.
     */
    private void save(List<RequestEvent> events, String streamName, Map<String, List<StoredRecord>> storedRecords) {
        events.forEach(event -> {
            var previousRecords = storedRecords.getOrDefault(streamName, new ArrayList<>());
            if (previousRecords.isEmpty()) {
                var record = storage.appendEvent(
                        streamName, Versions.UNDEFINED_STREAM, event.eventType(), event.eventContent());
                previousRecords.add(record);
            } else {
                var previousRecord = previousRecords.getLast();
                var record = storage.appendEvent(
                        streamName, previousRecord.version(), event.eventType(), event.eventContent());
                previousRecords.add(record);
            }
            storedRecords.put(streamName, previousRecords);
        });
    }

    @Nested
    class EmptyStorageTests {
        private final String streamName = "a-stream-name";

        @Test
        @DisplayName("Append to a stream that does not exist fails.")
        void appendToStreamThatDoesNotExist() {
            assertThatThrownBy(() -> storage.appendEvent(
                            "a-stream-that-does-not-exist", Versions.NEW_STREAM, "anything", "anything"))
                    .isInstanceOf(NoSuchStreamFailure.class);
        }

        @Test
        @DisplayName("Create a duplicate stream fails.")
        void createDuplicateStream() {
            storage.appendEvent(streamName, Versions.UNDEFINED_STREAM, "an-event-type", """
					{"key": "value"}""");

            assertThatThrownBy(() -> storage.appendEvent(streamName, Versions.UNDEFINED_STREAM, "anything", "{}"))
                    .isInstanceOf(DuplicateEventStreamFailure.class);
        }

        @Test
        @DisplayName("Create a stream successfully")
        void createStream() {
            var eventType = "an-event-type";
            var eventContent = """
					{"key": "value"}""";

            var record = storage.appendEvent(streamName, Versions.UNDEFINED_STREAM, eventType, eventContent);

            assertThat(record.streamName()).isEqualTo(streamName);
            assertThat(record.eventType()).isEqualTo(eventType);
            assertThat(record.eventContent()).isEqualTo(eventContent);
            assertThat(record.id()).isPositive();
            assertThat(record.version()).isEqualTo(Versions.NEW_STREAM);
        }

        @Test
        @DisplayName("Retrieve the event from a stream that does not exist fails.")
        void retrieveEvents() {
            assertThatThrownBy(() ->
                            storage.retrieveEvents(streamName, Collections.emptyList(), Versions.MIN, Versions.MAX))
                    .isInstanceOf(NoSuchStreamFailure.class);
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
                        new RequestEvent("first-event-id", EVENT_TYPE_A, """
						{"key1", "in-stream-one"}"""),
                        new RequestEvent("second-event-id", EVENT_TYPE_B, """
						{"key2", "in-stream-one"}"""),
                        new RequestEvent("third-event-id", EVENT_TYPE_A, """
						{"key3", "in-stream-one"}""")),
                STREAM_TWO,
                List.of(
                        new RequestEvent("first-event-id", EVENT_TYPE_A, """
						{"key1", "in-stream-two"}"""),
                        new RequestEvent("second-event-id", EVENT_TYPE_B, """
						{"key2", "in-stream-two"}"""),
                        new RequestEvent("third-event-id", EVENT_TYPE_A, """
						{"key3", "in-stream-two"}""")));

        private List<StoredRecord> storedRecords;

        @Test
        @DisplayName("Retrieve events from all streams after a given event")
        void retrieveEventsFromAllStreamsAfterGivenEvent() {
            var minRecordId = storedRecords.getFirst().id();
            var expected = storedRecords.stream().skip(1).toList();

            var records =
                    storage.retrieveEvents(minRecordId, Ids.MAX, Collections.emptyList(), Collections.emptyList());

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::insertedOn));
            assertThat(records.insertedOn()).isEqualTo(storedRecords.getLast().insertedOn());
        }

        @Test
        @DisplayName("Retrieve events from all streams and multiple event types")
        void retrieveEventsFromAllStreamsAndMultipleEventTypes() {
            var eventTypes = List.of(EVENT_TYPE_A, EVENT_TYPE_B);
            var expected = storedRecords;

            var records = storage.retrieveEvents(Ids.MIN, Ids.MAX, Collections.emptyList(), eventTypes);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::insertedOn));
            assertThat(records.insertedOn()).isEqualTo(storedRecords.getLast().insertedOn());
        }

        @Test
        @DisplayName("Retrieve events from all streams and one event type")
        void retrieveEventsFromAllStreamsAndOneEventType() {
            var eventTypes = List.of(EVENT_TYPE_A);
            var expected = storedRecords.stream()
                    .filter(record -> eventTypes.contains(record.eventType()))
                    .toList();

            var records = storage.retrieveEvents(Ids.MIN, Ids.MAX, Collections.emptyList(), eventTypes);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::insertedOn));
            assertThat(records.insertedOn()).isEqualTo(storedRecords.getLast().insertedOn());
        }

        @Test
        @DisplayName("Retrieve events from all streams and all types")
        void retrieveEventsFromAllStreamsAndTypes() {
            var expected = storedRecords;

            var records = storage.retrieveEvents(Ids.MIN, Ids.MAX, Collections.emptyList(), Collections.emptyList());

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::insertedOn));
            assertThat(records.insertedOn()).isEqualTo(storedRecords.getLast().insertedOn());
        }

        @Test
        @DisplayName("Retrieve events from all streams up to a given event")
        void retrieveEventsFromAllStreamsUptoGivenEvent() {
            var maxInclusiveRecordId =
                    storedRecords.get(storedRecords.size() - 2).id();
            var expected =
                    storedRecords.stream().limit(storedRecords.size() - 1).toList();

            var records = storage.retrieveEvents(
                    Ids.MIN, maxInclusiveRecordId, Collections.emptyList(), Collections.emptyList());

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::insertedOn));
            assertThat(records.insertedOn()).isEqualTo(storedRecords.getLast().insertedOn());
        }

        @Test
        @DisplayName("Retrieve events from all streams for id range edge cases")
        void retrieveEventsFromAllStreamsWithIdRangeEdgeCases() {
            assertAll(
                    "Retrieve events from all streams for id range edge cases",
                    () -> {
                        // start == end
                        var records = storage.retrieveEvents(1, 1, Collections.emptyList(), Collections.emptyList());

                        assertThat(records.records()).isEmpty();
                        assertThat(records.insertedOn())
                                .isEqualTo(storedRecords.getLast().insertedOn());
                    },
                    () -> {
                        // start > end
                        var records = storage.retrieveEvents(2, 1, Collections.emptyList(), Collections.emptyList());

                        assertThat(records.records()).isEmpty();
                        assertThat(records.insertedOn())
                                .isEqualTo(storedRecords.getLast().insertedOn());
                    },
                    () -> {
                        // start == end == min
                        var records = storage.retrieveEvents(
                                Ids.MIN, Ids.MIN, Collections.emptyList(), Collections.emptyList());

                        assertThat(records.records()).isEmpty();
                        assertThat(records.insertedOn())
                                .isEqualTo(storedRecords.getLast().insertedOn());
                    },
                    () -> {
                        // start == end == max
                        var records = storage.retrieveEvents(
                                Ids.MAX, Ids.MAX, Collections.emptyList(), Collections.emptyList());

                        assertThat(records.records()).isEmpty();
                        assertThat(records.insertedOn())
                                .isEqualTo(storedRecords.getLast().insertedOn());
                    });
        }

        @Test
        @DisplayName("Retrieve events from multiple streams and all types")
        void retrieveEventsFromMultipleStreamsAndTypes() {
            var streamNames = List.of(STREAM_ONE, STREAM_TWO);
            var expected = storedRecords;

            var records = storage.retrieveEvents(Ids.MIN, Ids.MAX, streamNames, Collections.emptyList());

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::insertedOn));
            assertThat(records.insertedOn()).isEqualTo(storedRecords.getLast().insertedOn());
        }

        @Test
        @DisplayName("Retrieve events from one stream and all types")
        void retrieveEventsFromOneStreamAndTypes() {
            var streamNames = List.of(STREAM_ONE);
            var expected = storedRecords.stream()
                    .filter(record -> streamNames.contains(record.streamName()))
                    .toList();

            var records = storage.retrieveEvents(Ids.MIN, Ids.MAX, streamNames, Collections.emptyList());

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::insertedOn));
            assertThat(records.insertedOn()).isEqualTo(storedRecords.getLast().insertedOn());
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

            var records = storage.retrieveEvents(Ids.MIN, Ids.MAX, streamNames, eventTypes);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::insertedOn));
            assertThat(records.insertedOn()).isEqualTo(storedRecords.getLast().insertedOn());
        }

        @Test
        @DisplayName("Retrieve events from some streams and some event types and within an id range")
        void retrieveEventsFromSomeStreamsAndSomeEventTypesWithinRange() {
            var eventTypes = List.of(EVENT_TYPE_A);
            var streamNames = List.of(STREAM_ONE);
            var minId = storedRecords.getFirst().id();
            var maxIdInclusive = minId + 2;
            var expected = storedRecords.stream()
                    .filter(record -> eventTypes.contains(record.eventType())
                            && streamNames.contains(record.streamName())
                            && (minId < record.id() && record.id() <= maxIdInclusive))
                    .toList();

            var records = storage.retrieveEvents(minId, maxIdInclusive, streamNames, eventTypes);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::insertedOn));
            assertThat(records.insertedOn()).isEqualTo(storedRecords.getLast().insertedOn());
        }

        @BeforeEach
        void seedEvents() {
            var storedRecords = mixAndSave(streams);
            this.storedRecords = flatten(storedRecords).stream()
                    .sorted(Comparator.comparing(StoredRecord::id))
                    .toList();
        }

        /**
         * Save the events to the store by mixing which stream gets appended.
         *
         * @param requests contain the events requested to be saved for each stream.
         * @return the {@link StoredRecord records} returned from {@link EventStorage#appendEvent(String, long, String,
         *     String)} for each stream.
         */
        private HashMap<String, List<StoredRecord>> mixAndSave(Map<String, List<RequestEvent>> requests) {
            var storedRecords = new HashMap<String, List<StoredRecord>>();
            int maxEventStream = requests.values().stream()
                    .map(List::size)
                    .max(Comparator.naturalOrder())
                    .orElseThrow();
            for (int index = 0; index < maxEventStream; index++) {
                for (var entry : requests.entrySet()) {
                    var streamName = entry.getKey();
                    var events = entry.getValue();
                    if (index < events.size()) {
                        save(List.of(events.get(index)), streamName, storedRecords);
                    }
                }
            }
            return storedRecords;
        }
    }

    @Nested
    class OneStreamStorageTests {
        private static final String EVENT_TYPE_A = "event-type-a";

        private static final String EVENT_TYPE_B = "event-type-b";

        private final String streamName = "a-stream-name";

        private final List<RequestEvent> events = List.of(
                new RequestEvent("first-event-id", EVENT_TYPE_A, """
				{"key1", "value"}"""),
                new RequestEvent("second-event-id", EVENT_TYPE_B, """
				{"key2", "value"}"""),
                new RequestEvent("third-event-id", EVENT_TYPE_A, """
				{"key3", "value"}"""));

        private List<StoredRecord> storedRecords;

        @Test
        @DisplayName("Append to stream with stale version fails.")
        void appendEventWithStaleVersion() {
            assertThatThrownBy(() -> storage.appendEvent(streamName, Versions.NEW_STREAM, "anything", "anything"))
                    .isInstanceOf(StaleVersionFailure.class);
        }

        @Test
        @DisplayName("Retrieve events from the stream successfully.")
        void retrieveEvents() {
            var expected = storedRecords;

            var records = storage.retrieveEvents(streamName, Collections.emptyList(), Versions.MIN, Versions.MAX);

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
        @DisplayName("Retrieve events from the stream after a specific version successfully.")
        void retrieveEventsAfterVersion() {
            var expected = List.of(storedRecords.get(1), storedRecords.get(2));

            var records = storage.retrieveEvents(
                    streamName,
                    Collections.emptyList(),
                    storedRecords.getFirst().version(),
                    Versions.MAX);

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
                    Versions.MIN,
                    storedRecords.get(1).version());

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
                                Versions.MAX);

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
                        var record =
                                storage.retrieveEvents(streamName, Collections.emptyList(), Versions.MAX, Versions.MAX);

                        assertThat(record.records()).isEmpty();
                        assertThat(record.version())
                                .isEqualTo(storedRecords.getLast().version());
                    });
        }

        @Test
        @DisplayName("Retrieve events from the stream with an event type and range filter successfully.")
        void retrieveEventsWithEventTypeAndRangeFilter() {
            var expected = List.of(storedRecords.get(1));

            var records = storage.retrieveEvents(
                    streamName, List.of(EVENT_TYPE_B), storedRecords.getFirst().version(), Versions.MAX);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::version));
            assertThat(records.version()).isEqualTo(storedRecords.getLast().version());
        }

        @Test
        @DisplayName("Retrieve specific one event type from the stream successfully.")
        void retrieveSpecificEventType() {
            var eventTypes = List.of(EVENT_TYPE_A);
            var expected = List.of(storedRecords.get(0), storedRecords.get(2));

            var records = storage.retrieveEvents(streamName, eventTypes, Versions.MIN, Versions.MAX);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::version));
            assertThat(records.version()).isEqualTo(storedRecords.getLast().version());
        }

        @Test
        @DisplayName("Retrieve specific multiple event types from the stream successfully.")
        void retrieveSpecificMultipleEventTypes() {
            var eventTypes = List.of(EVENT_TYPE_A, EVENT_TYPE_B);
            var expected = storedRecords;

            var records = storage.retrieveEvents(streamName, eventTypes, Versions.MIN, Versions.MAX);

            assertThat(records.records()).containsExactlyInAnyOrderElementsOf(expected);
            assertThat(records.records()).isSortedAccordingTo(Comparator.comparing(StoredRecord::version));
            assertThat(records.version()).isEqualTo(storedRecords.getLast().version());
        }

        @BeforeEach
        void seedEvents() {
            var storedRecords = new HashMap<String, List<StoredRecord>>();
            save(this.events, streamName, storedRecords);
            this.storedRecords = flatten(storedRecords).stream()
                    .sorted(Comparator.comparing(StoredRecord::id))
                    .toList();
        }
    }
}
