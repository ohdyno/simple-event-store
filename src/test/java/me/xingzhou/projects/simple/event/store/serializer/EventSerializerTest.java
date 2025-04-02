package me.xingzhou.projects.simple.event.store.serializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import me.xingzhou.projects.simple.event.store.Event;
import me.xingzhou.projects.simple.event.store.serializer.events.FooEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class EventSerializerTest {
    private EventSerializer subject;

    protected abstract EventSerializer createSerializer();

    @BeforeEach
    void setUp() {
        subject = createSerializer();
    }

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
        var result = assertThrows(UnknownEventTypeFailure.class, () -> subject.deserialize("unknown-event-type", "{}"));
        assertThat(result.getMessage())
                .isEqualTo("""
                        Unknown event type: 'unknown-event-type'""");
    }

    @Test
    void extractDefinedEventsFromApplyMethods() {
        assertThat(subject.extractDefinedEventsFromApplyMethods(new Object() {
                    public void apply(FooEvent event) {}
                }))
                .containsOnly(subject.getTypeName(FooEvent.class));

        assertThat(subject.extractDefinedEventsFromApplyMethods(new Object() {
                    public void apply(Event event) {}
                }))
                .isEmpty();

        assertThat(subject.extractDefinedEventsFromApplyMethods(new Object() {
                    public void apply(Event event) {}

                    public void apply(FooEvent event) {}
                }))
                .isEmpty();
    }

    @Test
    void extractDefinedEventsFromApplyMethodsIgnoresEventInterface() {
        assertThat(subject.extractDefinedEventsFromApplyMethods(new Object() {
                    public void apply(Event event) {}
                }))
                .isEmpty();

        assertThat(subject.extractDefinedEventsFromApplyMethods(new Object() {
                    public void apply(Event event) {}

                    public void apply(FooEvent event) {}
                }))
                .isEmpty();
    }
}
