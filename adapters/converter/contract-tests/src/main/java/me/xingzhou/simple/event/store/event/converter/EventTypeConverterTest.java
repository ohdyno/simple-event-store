package me.xingzhou.simple.event.store.event.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collection;
import me.xingzhou.simple.event.store.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class EventTypeConverterTest {
    protected EventTypeConverter subject;

    protected abstract EventTypeConverter createConverter();

    protected abstract Collection<Class<? extends Event>> events();

    @Test
    void convert() {
        events().forEach(event -> {
            var eventType = subject.convert(event);
            var convertedEvent = subject.convert(eventType);

            assertThat(convertedEvent)
                    .as("converting to and from: " + event.getSimpleName())
                    .isEqualTo(event);
        });
    }

    @BeforeEach
    void setUp() {
        subject = createConverter();
    }
}
