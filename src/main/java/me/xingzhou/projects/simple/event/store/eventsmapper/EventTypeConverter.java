package me.xingzhou.projects.simple.event.store.eventsmapper;

public interface EventTypeConverter {
    String convert(Class<?> event);

    Class<?> convert(String eventType);
}
