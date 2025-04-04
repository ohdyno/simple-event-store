package me.xingzhou.projects.simple.event.store.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
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

            assertThat(version).isEqualTo(storage.initialVersion());
        }
    }

    @Nested
    class OneStreamStorageTests {
        private final String streamName = "a-stream-name";
        private final List<RequestEvent> events =
                List.of(new RequestEvent("an-event-id", "an-event-type", """
                    {"key", "value"}"""));

        @BeforeEach
        void setUp() {
            events.forEach(event ->
                    storage.createStream(streamName, event.eventId(), event.eventType(), event.eventContent()));
        }

        @Test
        @DisplayName("Create a duplicate stream fails.")
        void createDuplicateStream() {
            assertThatThrownBy(() -> storage.createStream(streamName, "an-event-id", "an-event-type", "{}"))
                    .isInstanceOf(DuplicateEventStreamFailure.class);
        }
    }

    private record RequestEvent(String eventId, String eventType, String eventContent) {}
}
