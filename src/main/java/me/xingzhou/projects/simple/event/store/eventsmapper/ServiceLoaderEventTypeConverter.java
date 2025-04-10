package me.xingzhou.projects.simple.event.store.eventsmapper;

import java.util.HashMap;
import java.util.ServiceLoader;
import me.xingzhou.projects.simple.event.store.Event;

public class ServiceLoaderEventTypeConverter implements EventTypeConverter {
    private final MapBasedEventTypeConverter delegate;

    public ServiceLoaderEventTypeConverter() {
        var mapping = new HashMap<String, Class<? extends Event>>();
        for (var event : ServiceLoader.load(Event.class)) {
            var klass = event.getClass();
            mapping.put(klass.getSimpleName(), klass);
        }
        this.delegate = new MapBasedEventTypeConverter(mapping);
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
