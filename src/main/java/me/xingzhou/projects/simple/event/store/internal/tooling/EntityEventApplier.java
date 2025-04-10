package me.xingzhou.projects.simple.event.store.internal.tooling;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import java.lang.reflect.Method;
import java.util.Arrays;
import me.xingzhou.projects.simple.event.store.EventRecord;
import me.xingzhou.projects.simple.event.store.entities.EventSourceEntity;

public class EntityEventApplier {
    public static <T> void apply(EventRecord record, T entity) {
        handleExceptions(() -> {
            var method = getMethod(entity.getClass(), record.event().getClass());
            method.invoke(entity, record.event());
        });
    }

    private static Method getMethod(Class<?> entity, Class<?> event) throws NoSuchMethodException {
        return Arrays.stream(entity.getMethods())
                .filter(EventSourceEntity::isApplyMethod)
                .filter(method -> method.getParameterTypes()[0].isAssignableFrom(event))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException(
                        "No such method: public void apply(" + event.getName() + ") on " + entity.getName()));
    }
}
