package me.xingzhou.projects.simple.event.store;

public interface EventSerializer {
  SerializedEvent serialize(Event event);
}
