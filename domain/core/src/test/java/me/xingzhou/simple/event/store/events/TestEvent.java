package me.xingzhou.simple.event.store.events;

import com.google.auto.service.AutoService;
import me.xingzhou.simple.event.store.Event;

@AutoService(Event.class)
public record TestEvent(String id) implements Event {
    public TestEvent() {
        this(null);
    }
}
