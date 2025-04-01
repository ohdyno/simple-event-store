package me.xingzhou.projects.simple.event.store.serializer.adapters;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ServiceLoader;
import java.util.stream.StreamSupport;
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
        var eventType = getTypeName(event.getClass());
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
        return klass.getSimpleName();
    }

    @Override
    public Event deserialize(String eventType, String eventJson) {
        return StreamSupport.stream(serviceProvider.spliterator(), false)
                .filter(e -> getTypeName(e.getClass()).equals(eventType))
                .findFirst()
                .map(event -> handleExceptions(() -> objectMapper.readValue(eventJson, event.getClass())))
                .orElseThrow(() -> new DeserializationFailure(eventType));
    }
}
