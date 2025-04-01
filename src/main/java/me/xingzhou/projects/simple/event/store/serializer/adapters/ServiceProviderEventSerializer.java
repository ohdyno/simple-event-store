package me.xingzhou.projects.simple.event.store.serializer.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ServiceLoader;
import me.xingzhou.projects.simple.event.store.Event;
import me.xingzhou.projects.simple.event.store.internal.tooling.ThrowableSupplier;
import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.serializer.SerializedEvent;

public class ServiceProviderEventSerializer implements EventSerializer {
    private final ServiceLoader<Event> serviceProvider = ServiceLoader.load(Event.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public SerializedEvent serialize(Event event) {
        var eventJson = handleExceptions(() -> objectMapper.writeValueAsString(event));
        var eventType = event.getClass().getSimpleName();
        return new SerializedEvent(eventType, eventJson);
    }

    private <R> R handleExceptions(ThrowableSupplier<R> fn) {
        try {
            return fn.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTypeName(Class<? extends Event> klass) {
        return "";
    }

    @Override
    public Event deserialize(String eventType, String eventJson) {
        return null;
    }
}
