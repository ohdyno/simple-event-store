package me.xingzhou.simple.event.store.events;

import java.util.Map;
import me.xingzhou.simple.event.store.event.converter.MapBackedEventTypeConverter;

public class TestEventTypeConverter extends MapBackedEventTypeConverter {
    public TestEventTypeConverter() {
        super(Map.of("TestEvent", TestEvent.class, "AnotherEvent", AnotherEvent.class));
    }
}
