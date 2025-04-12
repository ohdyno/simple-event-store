package me.xingzhou.simple.event.store.serializer.adapters;

import static me.xingzhou.simple.event.store.serializer.adapters.internal.CheckedExceptionHandlers.handleExceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.event.converter.EventTypeConverter;
import me.xingzhou.simple.event.store.serializer.EventSerializer;
import me.xingzhou.simple.event.store.serializer.SerializedEvent;

public class JacksonEventSerializer implements EventSerializer {
    public static final ObjectMapper DEFAULT_OBJECT_MAPPER = new ObjectMapper();

    private final ObjectMapper objectMapper;
    private final EventTypeConverter converter;

    public JacksonEventSerializer(EventTypeConverter mapper) {
        this(DEFAULT_OBJECT_MAPPER, mapper);
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
