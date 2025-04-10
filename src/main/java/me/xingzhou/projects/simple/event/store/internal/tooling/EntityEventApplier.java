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
            var method = getMethod(entity, record.event().getClass());
            method.invoke(entity, record.event());
        });
    }

    private static <T> Method getMethod(T entity, Class<?> eventClass) throws NoSuchMethodException {
        List<Class<?>> allPossibleClasses = new ArrayList<>();
        allPossibleClasses.add(eventClass);
        allPossibleClasses.addAll(ClassUtils.getAllSuperclasses(eventClass));
        allPossibleClasses.addAll(ClassUtils.getAllInterfaces(eventClass));

        for (var cls : allPossibleClasses) {
            try {
                return entity.getClass().getMethod("apply", cls);
            } catch (NoSuchMethodException ignored) {
            }
        }
        throw new NoSuchMethodException("No such method: public void apply(" + eventClass.getName() + ") in "
                + entity.getClass().getName());
    }
}
