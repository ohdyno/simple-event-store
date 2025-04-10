package me.xingzhou.projects.simple.event.store.entities;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import me.xingzhou.projects.simple.event.store.Event;

public interface EventSourceEntity {
    String APPLY_METHOD_NAME = "apply";

    default List<Class<?>> extractEventTypesFromApplyMethods() {
        Predicate<Method> isApplyMethod = (method) -> method.getName().equals(APPLY_METHOD_NAME)
                && method.getParameterCount() > 0
                && Event.class.isAssignableFrom(method.getParameterTypes()[0]);
        return Arrays.stream(this.getClass().getMethods())
                .filter(isApplyMethod)
                .map(method -> method.getParameterTypes()[0])
                .collect(Collectors.toUnmodifiableList());
    }
}
