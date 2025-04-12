package me.xingzhou.simple.event.store.serializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import me.xingzhou.simple.event.store.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class EventSerializerTest {
    private EventSerializer subject;

    protected abstract EventSerializer createSerializer();

    protected abstract Event createTestEvent(String id);

    @Test
    void deserialize() {
        var event = createTestEvent("some-event-id");
        var serialized = subject.serialize(event);

        var result = subject.deserialize(serialized.eventType(), serialized.eventJson());

        assertThat(result).isEqualTo(event);
    }

    @Test
    void deserializeAnUnknownEvent() {
        var result = assertThrows(UnknownEventTypeFailure.class, () -> subject.deserialize("unknown-event-type", "{}"));
        assertThat(result.getMessage()).isEqualTo("""
				Unknown event type: 'unknown-event-type'""");
    }

    @BeforeEach
    void setUp() {
        subject = createSerializer();
    }
}
