package me.xingzhou.simple.event.store.event.converter;

public interface EventTypeConverter {
    String convert(Class<?> event);

    Class<?> convert(String eventType);
}
