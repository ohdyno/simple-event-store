package me.xingzhou.projects.simple.event.store.serializer.adapters.events;

import com.google.auto.service.AutoService;
import me.xingzhou.projects.simple.event.store.Event;

@AutoService(Event.class)
public record FooEvent(String id) implements Event {
    public FooEvent() {
        this(null);
    }
}
