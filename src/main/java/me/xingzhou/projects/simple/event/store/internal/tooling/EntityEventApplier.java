package me.xingzhou.projects.simple.event.store.internal.tooling;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import java.lang.reflect.Method;
import me.xingzhou.projects.simple.event.store.Event;
import me.xingzhou.projects.simple.event.store.EventRecord;

public class EntityEventApplier {
    public static <T> void apply(EventRecord record, T entity) {
        handleExceptions(() -> {
            var method = getMethod(entity, record.event().getClass());
            method.invoke(entity, record.event());
        });
    }

    private static <T> Method getMethod(T entity, Class<?> eventClass) throws NoSuchMethodException {
        try {
            return entity.getClass().getDeclaredMethod("apply", eventClass);
        } catch (NoSuchMethodException e) {
            if (eventClass.equals(Event.class)) {
                throw e;
            }
            return getMethod(entity, Event.class);
        }
    }
}
