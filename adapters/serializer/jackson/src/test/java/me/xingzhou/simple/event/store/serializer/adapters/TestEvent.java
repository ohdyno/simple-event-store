package me.xingzhou.simple.event.store.serializer.adapters;

import me.xingzhou.simple.event.store.Event;

public record TestEvent(String id) implements Event {}
