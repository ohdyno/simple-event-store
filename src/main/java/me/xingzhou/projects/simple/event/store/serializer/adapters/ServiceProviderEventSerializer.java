package me.xingzhou.projects.simple.event.store.serializer.adapters;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;
import me.xingzhou.projects.simple.event.store.Event;
import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.serializer.SerializedEvent;
import me.xingzhou.projects.simple.event.store.serializer.UnknownEventTypeFailure;

public class ServiceProviderEventSerializer implements EventSerializer {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final HashMap<String, Class<? extends Event>> definedEvents = new HashMap<>();

    public ServiceProviderEventSerializer() {
        var serviceLoader = ServiceLoader.load(Event.class);
        for (var event : serviceLoader) {
            var klass = event.getClass();
            definedEvents.put(getTypeName(klass), klass);
        }
    }

    @Override
    public Event deserialize(String eventType, String eventJson) {
        if (definedEvents.containsKey(eventType)) {
            return handleExceptions(() -> objectMapper.readValue(eventJson, definedEvents.get(eventType)));
        }
        throw new UnknownEventTypeFailure(eventType);
    }

    @Override
    public List<String> extractDefinedEventsFromApplyMethods(Object object) {
        var allTypes = Arrays.stream(object.getClass().getMethods())
                .filter(method -> "apply".equals(method.getName()) && method.getParameterCount() > 0)
                .map(method -> method.getParameters()[0].getType().getSimpleName())
                .filter(typeName -> definedEvents.containsKey(typeName) || "Event".equals(typeName))
                .collect(Collectors.toSet());
        if (allTypes.contains("Event")) {
            return Collections.emptyList();
        }
        return List.copyOf(allTypes);
    }

    @Override
    public String getTypeName(Class<? extends Event> klass) {
        return klass.getSimpleName();
    }

    @Override
    public SerializedEvent serialize(Event event) {
        var eventJson = handleExceptions(() -> objectMapper.writeValueAsString(event));
        var eventType = getTypeName(event.getClass());
        return new SerializedEvent(eventType, eventJson);
    }
}
