package me.xingzhou.simple.event.store.serializer.adapters;

import me.xingzhou.simple.event.store.event.converter.ServiceLoaderEventTypeConverter;
import me.xingzhou.simple.event.store.serializer.EventSerializer;
import me.xingzhou.simple.event.store.serializer.EventSerializerTest;

public class JacksonEventSerializerTest extends EventSerializerTest {
    @Override
    protected EventSerializer createSerializer() {
        return new JacksonEventSerializer(new ServiceLoaderEventTypeConverter());
    }
}
