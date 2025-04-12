package me.xingzhou.simple.event.store.internal.tooling;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.entities.EventSourceEntity;
import me.xingzhou.simple.event.store.event.converter.EventTypeConverter;

public class EventTypesExtractor {
    private static boolean isApplyMethod(Method method) {
        return EventSourceEntity.APPLY_METHOD_NAME.equals(method.getName())
                && method.getParameterCount() > 0
                && Event.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    private final EventTypeConverter converter;

    public EventTypesExtractor(EventTypeConverter converter) {
        this.converter = converter;
    }

    public List<String> extract(EventSourceEntity entity) {
        return extractTypes(entity).stream()
                .filter(Predicate.not(klass -> klass.equals(Event.class)))
                .map(converter::convert)
                .toList();
    }

    public List<Class<?>> extractTypes(EventSourceEntity entity) {
        return Arrays.stream(entity.getClass().getMethods())
                .filter(EventTypesExtractor::isApplyMethod)
                .map(method -> method.getParameterTypes()[0])
                .collect(Collectors.toUnmodifiableList());
    }
}
