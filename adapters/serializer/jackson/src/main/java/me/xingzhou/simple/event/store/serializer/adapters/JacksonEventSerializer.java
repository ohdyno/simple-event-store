package me.xingzhou.simple.event.store.serializer.adapters;

import static me.xingzhou.simple.event.store.serializer.adapters.internal.CheckedExceptionHandlers.handleExceptions;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.event.converter.EventTypeConverter;
import me.xingzhou.simple.event.store.serializer.EventSerializer;
import me.xingzhou.simple.event.store.serializer.SerializedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JacksonEventSerializer implements EventSerializer {
    private static final Logger log = LoggerFactory.getLogger(JacksonEventSerializer.class);

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
        log.debug("deserialize event of type: {}", eventType);
        var event = (Event) handleExceptions(() -> objectMapper.readValue(eventJson, converter.convert(eventType)));
        log.debug("deserialized event: {}", event);
        return event;
    }

    @Override
    public SerializedEvent serialize(Event event) {
        log.debug("serialize event: {}", event);
        var eventJson = handleExceptions(() -> objectMapper.writeValueAsString(event));
        var eventType = converter.convert(event.getClass());
        log.debug("serialized event of type: {}", eventType);
        return new SerializedEvent(eventType, eventJson);
    }
}
