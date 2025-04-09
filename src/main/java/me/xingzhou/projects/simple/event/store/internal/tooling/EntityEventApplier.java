package me.xingzhou.projects.simple.event.store.internal.tooling;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import me.xingzhou.projects.simple.event.store.EventRecord;

public class EntityEventApplier {
    public static <T> void apply(EventRecord record, T entity) {
        handleExceptions(() -> {
            var lookup = MethodHandles.publicLookup();
            var methodType = MethodType.methodType(void.class, record.event().getClass());
            var applyMethod = lookup.findVirtual(entity.getClass(), "apply", methodType);
            applyMethod.invoke(entity, record.event());
        });
    }
}
