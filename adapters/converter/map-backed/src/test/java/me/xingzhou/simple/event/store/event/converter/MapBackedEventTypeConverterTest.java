package me.xingzhou.simple.event.store.event.converter;

import java.util.Collection;
import java.util.Map;
import me.xingzhou.simple.event.store.Event;

class MapBackedEventTypeConverterTest extends EventTypeConverterTest {

    private final Map<String, Class<? extends Event>> mapping = Map.of("Foo", Foo.class, "Bar", Bar.class);

    @Override
    protected EventTypeConverter createConverter() {
        return new MapBackedEventTypeConverter(mapping);
    }

    @Override
    protected Collection<Class<? extends Event>> events() {
        return mapping.values();
    }
}
