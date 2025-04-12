package me.xingzhou.simple.event.store.event.converter;

import java.util.Collection;
import java.util.Set;
import me.xingzhou.simple.event.store.Event;

class ServiceLoaderEventTypeConverterTest extends EventTypeConverterTest {

    @Override
    protected EventTypeConverter createConverter() {
        return new ServiceLoaderEventTypeConverter();
    }

    @Override
    protected Collection<Class<? extends Event>> events() {
        return Set.of(TestEvent.class);
    }
}
