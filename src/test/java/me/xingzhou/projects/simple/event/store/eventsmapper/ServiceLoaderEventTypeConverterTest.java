package me.xingzhou.projects.simple.event.store.eventsmapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import me.xingzhou.projects.simple.event.store.serializer.events.FooEvent;
import org.junit.jupiter.api.Test;

class ServiceLoaderEventTypeConverterTest {

    private EventTypeConverter subject = new ServiceLoaderEventTypeConverter();

    @Test
    void convert() {
        var event = new FooEvent("foo-event-id");

        var eventType = subject.convert(event.getClass());
        var convertedEvent = subject.convert(eventType);

        assertThat(convertedEvent).isEqualTo(event.getClass());
    }
}
