package me.xingzhou.projects.simple.event.store.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
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
    }

    @Nested
    class OneStreamStorageTests {
        private final String streamName = "a-stream-name";
        private final RequestEvent event =
                new RequestEvent("an-event-id", "an-event-type", """
                {"key", "value"}""");

        @BeforeEach
        void setUp() {
            storage.createStream(streamName, event.eventId(), event.eventType(), event.eventContent());
        }

        @Test
        @DisplayName("Retrieve the event from the stream.")
        void retrieveEvents() {
            var records = storage.retrieveEvents(
                    streamName, Collections.emptyList(), storage.undefinedVersion(), storage.exclusiveMaxVersion());

            assertThat(records.records())
                    .containsOnly(new StoredRecord(
                            event.eventType(), event.eventContent(), streamName, storage.newStreamVersion()));
        }

        @Test
        @DisplayName("Create a duplicate stream fails.")
        void createDuplicateStream() {
            assertThatThrownBy(() ->
                            storage.createStream(streamName, event.eventId(), event.eventType(), event.eventContent))
                    .isInstanceOf(DuplicateEventStreamFailure.class);
        }
    }

    private record RequestEvent(String eventId, String eventType, String eventContent) {}
}
