package me.xingzhou.simple.event.store.events;

import me.xingzhou.simple.event.store.Event;

public record AnotherEvent(String id) implements Event {
    public AnotherEvent() {
        this(null);
    }
}
