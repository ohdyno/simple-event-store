package me.xingzhou.projects.simple.event.store.internal.tooling;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import me.xingzhou.projects.simple.event.store.EventRecord;
import me.xingzhou.projects.simple.event.store.entities.EventSourceEntity;
import me.xingzhou.projects.simple.event.store.entities.EventTypesExtractor;

public class EntityEventApplier {
    public static <T extends EventSourceEntity> void apply(EventRecord record, T entity) {
        handleExceptions(() -> {
            var parameterType = getMethod(entity, record.event().getClass());
            var method = entity.getClass().getMethod(EventSourceEntity.APPLY_METHOD_NAME, parameterType);
            method.invoke(entity, record.event());
        });
    }

    private static Class<?> getMethod(EventSourceEntity entity, Class<?> event) throws NoSuchMethodException {
        return new EventTypesExtractor()
                .extract(entity).stream()
                        .filter(klass -> klass.isAssignableFrom(event))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchMethodException("No such method: public void apply("
                                + event.getName() + ") on " + entity.getClass().getName()));
    }
}
