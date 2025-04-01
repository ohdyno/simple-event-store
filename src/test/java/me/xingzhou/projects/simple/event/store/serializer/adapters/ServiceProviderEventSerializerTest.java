package me.xingzhou.projects.simple.event.store.serializer.adapters;

import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.serializer.EventSerializerTest;

public class ServiceProviderEventSerializerTest extends EventSerializerTest {
    @Override
    protected EventSerializer createSerializer() {
        return new ServiceProviderEventSerializer();
    }
}
