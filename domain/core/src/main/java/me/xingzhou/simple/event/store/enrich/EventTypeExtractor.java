package me.xingzhou.simple.event.store.enrich;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.entities.EventSourceEntity;

public class EventTypeExtractor {
    public List<Class<?>> extractTypes(EventSourceEntity entity) {
        return Arrays.stream(entity.getClass().getMethods())
                .filter(this::isApplyMethod)
                .map(method -> method.getParameterTypes()[0])
                .distinct()
                .collect(Collectors.toUnmodifiableList());
    }

    private boolean isApplyMethod(Method method) {
        return EventSourceEntity.APPLY_METHOD_NAME.equals(method.getName())
                && method.getParameterCount() > 0
                && Event.class.isAssignableFrom(method.getParameterTypes()[0]);
    }
}
