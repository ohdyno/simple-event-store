package me.xingzhou.projects.simple.event.store.internal.tooling;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import me.xingzhou.projects.simple.event.store.EventRecord;
import org.apache.commons.lang3.ClassUtils;

public class EntityEventApplier {
    public static <T> void apply(EventRecord record, T entity) {
        handleExceptions(() -> {
            var method = getMethod(entity.getClass(), record.event().getClass());
            method.invoke(entity, record.event());
        });
    }

    private static Method getMethod(Class<?> entity, Class<?> event) throws NoSuchMethodException {
        List<Class<?>> eventClasses = new ArrayList<>();
        eventClasses.add(event);
        eventClasses.addAll(ClassUtils.getAllSuperclasses(event));
        eventClasses.addAll(ClassUtils.getAllInterfaces(event));

        for (var aClass : eventClasses) {
            try {
                return entity.getMethod("apply", aClass);
            } catch (NoSuchMethodException ignored) {
            }
        }

        throw new NoSuchMethodException(
                "No such method: public void apply(" + event.getName() + ") on " + entity.getName());
    }
}
