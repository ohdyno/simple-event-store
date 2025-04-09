package me.xingzhou.projects.simple.event.store.events;

import com.google.auto.service.AutoService;
import me.xingzhou.projects.simple.event.store.Event;

@AutoService(Event.class)
public record TestEvent(String id) implements Event {
    public TestEvent() {
        this(null);
    }
}
