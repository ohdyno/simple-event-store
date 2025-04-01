package me.xingzhou.projects.simple.event.store.serializer.adapters.events;

import me.xingzhou.projects.simple.event.store.Event;

public record FooEvent(String id) implements Event {}
