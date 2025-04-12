package me.xingzhou.simple.event.store.event.converter;

import java.util.HashMap;
import java.util.ServiceLoader;
import me.xingzhou.simple.event.store.Event;

public class ServiceLoaderEventTypeConverter implements EventTypeConverter {
    private final MapBackedEventTypeConverter delegate;

    public ServiceLoaderEventTypeConverter() {
        var mapping = new HashMap<String, Class<? extends Event>>();
        for (var event : ServiceLoader.load(Event.class)) {
            var klass = event.getClass();
            mapping.put(klass.getSimpleName(), klass);
        }
        this.delegate = new MapBackedEventTypeConverter(mapping);
    }

    @Override
    public String convert(Class<?> event) {
        return delegate.convert(event);
    }

    @Override
    public Class<?> convert(String eventType) {
        return delegate.convert(eventType);
    }
}
