package me.xingzhou.projects.simple.event.store.entities;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.xingzhou.projects.simple.event.store.Event;

public class EventTypesExtractor {
    private static boolean isApplyMethod(Method method) {
        return EventSourceEntity.APPLY_METHOD_NAME.equals(method.getName())
                && method.getParameterCount() > 0
                && Event.class.isAssignableFrom(method.getParameterTypes()[0]);
    }

    public List<Class<?>> extract(EventSourceEntity entity) {
        return Arrays.stream(entity.getClass().getMethods())
                .filter(EventTypesExtractor::isApplyMethod)
                .map(method -> method.getParameterTypes()[0])
                .collect(Collectors.toUnmodifiableList());
    }
}
