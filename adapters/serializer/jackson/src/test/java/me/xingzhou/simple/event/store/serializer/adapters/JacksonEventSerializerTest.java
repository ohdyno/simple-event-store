package me.xingzhou.simple.event.store.serializer.adapters;

import java.util.Map;
import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.event.converter.MapBackedEventTypeConverter;
import me.xingzhou.simple.event.store.serializer.EventSerializer;
import me.xingzhou.simple.event.store.serializer.EventSerializerTest;

public class JacksonEventSerializerTest extends EventSerializerTest {
    @Override
    protected EventSerializer createSerializer() {
        return new JacksonEventSerializer(new MapBackedEventTypeConverter(Map.of("TestEvent", TestEvent.class)));
    }

    @Override
    protected Event createTestEvent(String id) {
        return new TestEvent(id);
    }
}
