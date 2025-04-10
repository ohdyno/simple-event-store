package me.xingzhou.projects.simple.event.store.serializer.adapters;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import me.xingzhou.projects.simple.event.store.Event;
import me.xingzhou.projects.simple.event.store.entities.EventSourceEntity;
import me.xingzhou.projects.simple.event.store.eventsmapper.EventTypeMapper;
import me.xingzhou.projects.simple.event.store.eventsmapper.ServiceLoaderEventTypeMapper;
import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.serializer.SerializedEvent;
import me.xingzhou.projects.simple.event.store.serializer.UnknownEventTypeFailure;

public class JacksonEventSerializer implements EventSerializer {
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

    private final Map<String, Class<? extends Event>> definedEvents;

    private final ObjectMapper objectMapper;

    public JacksonEventSerializer() {
        this(DEFAULT_OBJECT_MAPPER, new ServiceLoaderEventTypeMapper());
    }

    public JacksonEventSerializer(EventTypeMapper mapper) {
        this(DEFAULT_OBJECT_MAPPER, mapper);
    }

    public JacksonEventSerializer(ObjectMapper objectMapper) {
        this(objectMapper, new ServiceLoaderEventTypeMapper());
    }

    public JacksonEventSerializer(ObjectMapper objectMapper, EventTypeMapper mapper) {
        this.objectMapper = objectMapper;
        this.definedEvents = mapper.getMapping();
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
                .filter(EventSourceEntity::isApplyMethod)
                .map(this::extractFirstParameterType)
                .filter(this::isDefinedEventType)
                .collect(Collectors.toSet());
        if (allTypes.contains(Event.class)) {
            return Collections.emptyList();
        }
        return allTypes.stream().map(this::getDefinedEventName).toList();
    }

    @Override
    public String getDefinedEventName(Class<?> klass) {
        return definedEvents.entrySet().stream()
                .filter(entry -> klass.equals(entry.getValue()))
                .findFirst()
                .orElseThrow(() -> new UnknownEventTypeFailure(klass))
                .getKey();
    }

    @Override
    public SerializedEvent serialize(Event event) {
        var eventJson = handleExceptions(() -> objectMapper.writeValueAsString(event));
        var eventType = getDefinedEventName(event.getClass());
        return new SerializedEvent(eventType, eventJson);
    }

    private Class<?> extractFirstParameterType(Method method) {
        return method.getParameterTypes()[0];
    }

    private boolean isDefinedEventType(Class<?> parameterClass) {
        return definedEvents.containsValue(parameterClass) || Event.class.equals(parameterClass);
    }
}
