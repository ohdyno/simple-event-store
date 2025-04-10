package me.xingzhou.projects.simple.event.store.serializer.adapters;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import me.xingzhou.projects.simple.event.store.Event;
import me.xingzhou.projects.simple.event.store.eventsmapper.EventTypeConverter;
import me.xingzhou.projects.simple.event.store.eventsmapper.ServiceLoaderEventTypeConverter;
import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.serializer.SerializedEvent;

public class JacksonEventSerializer implements EventSerializer {
    private static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

    private final ObjectMapper objectMapper;
    private final EventTypeConverter converter;

    public JacksonEventSerializer() {
        this(DEFAULT_OBJECT_MAPPER, new ServiceLoaderEventTypeConverter());
    }

    public JacksonEventSerializer(EventTypeConverter mapper) {
        this(DEFAULT_OBJECT_MAPPER, mapper);
    }

    public JacksonEventSerializer(ObjectMapper objectMapper) {
        this(objectMapper, new ServiceLoaderEventTypeConverter());
    }

    public JacksonEventSerializer(ObjectMapper objectMapper, EventTypeConverter converter) {
        this.objectMapper = objectMapper;
        this.converter = converter;
    }

    @Override
    public Event deserialize(String eventType, String eventJson) {
        return (Event) handleExceptions(() -> objectMapper.readValue(eventJson, converter.convert(eventType)));
    }

    @Override
    public SerializedEvent serialize(Event event) {
        var eventJson = handleExceptions(() -> objectMapper.writeValueAsString(event));
        var eventType = converter.convert(event.getClass());
        return new SerializedEvent(eventType, eventJson);
    }
}
