package me.xingzhou.simple.event.store.events;

import me.xingzhou.simple.event.store.Event;

public record TestEvent(String id) implements Event {
    public TestEvent() {
        this(null);
    }
}
