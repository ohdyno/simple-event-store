package me.xingzhou.projects.simple.event.store.serializer.adapters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import me.xingzhou.projects.simple.event.store.serializer.adapters.events.FooEvent;
import org.junit.jupiter.api.Test;

public class ServiceProviderEventSerializerTest {
    private final ServiceProviderEventSerializer subject = new ServiceProviderEventSerializer();

    @Test
    void serialize() {
        var result = subject.serialize(new FooEvent("foo-event-id"));
        assertThat(result.eventType()).isEqualTo(FooEvent.class.getSimpleName());
        assertThat(result.eventJson()).isEqualTo("""
                {"id":"foo-event-id"}
                """.trim());
    }

    @Test
    void deserialize() {
        var event = new FooEvent("foo-event-id");
        var serialized = subject.serialize(event);

        var result = subject.deserialize(serialized.eventType(), serialized.eventJson());

        assertThat(result).isEqualTo(event);
    }

    @Test
    void getTypeName() {
        var event = new FooEvent("foo-event-id");
        var serialized = subject.serialize(event);

        var result = subject.getTypeName(event.getClass());

        assertThat(result).isEqualTo(serialized.eventType());
    }

    @Test
    void deserializeAnUnknownEvent() {
        var result = assertThrows(DeserializationFailure.class, () -> subject.deserialize("unknown-event-type", "{}"));
        assertThat(result.getMessage())
                .isEqualTo("""
                Unable to deserialize "unknown-event-type" to a known type.""");
    }
}
