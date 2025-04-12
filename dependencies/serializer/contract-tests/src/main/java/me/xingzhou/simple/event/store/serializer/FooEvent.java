package me.xingzhou.simple.event.store.serializer;

import com.google.auto.service.AutoService;
import me.xingzhou.simple.event.store.Event;

@AutoService(Event.class)
public record FooEvent(String id) implements Event {
    public FooEvent() {
        this(null);
    }
}
