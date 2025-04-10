package me.xingzhou.projects.simple.event.store.eventsmapper;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import me.xingzhou.projects.simple.event.store.Event;

public class ServiceLoaderEventTypeMapper implements EventTypeMapper {
    @Override
    public Map<String, Class<? extends Event>> getMapping() {
        var mapping = new HashMap<String, Class<? extends Event>>();
        for (var event : ServiceLoader.load(Event.class)) {
            var klass = event.getClass();
            mapping.put(klass.getSimpleName(), klass);
        }
        return mapping;
    }
}
