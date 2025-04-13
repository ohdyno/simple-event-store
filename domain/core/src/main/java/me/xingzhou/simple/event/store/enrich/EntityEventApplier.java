package me.xingzhou.simple.event.store.enrich;

import static me.xingzhou.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import me.xingzhou.simple.event.store.entities.EventSourceEntity;

public class EntityEventApplier {

    private final EventTypesExtractor eventTypesExtractor;

    public EntityEventApplier(EventTypesExtractor eventTypesExtractor) {
        this.eventTypesExtractor = eventTypesExtractor;
    }

    public <T extends EventSourceEntity> void apply(EventRecord record, T entity) {
        handleExceptions(() -> {
            var parameterType = getMethod(entity, record.event().getClass());
            try {
                var method = entity.getClass().getMethod(EventSourceEntity.APPLY_METHOD_NAME, parameterType);
                method.invoke(entity, record.event());
            } catch (NoSuchMethodException e) {
                var method = entity.getClass()
                        .getMethod(
                                EventSourceEntity.APPLY_METHOD_NAME,
                                parameterType,
                                record.details().getClass());
                method.invoke(entity, record.event(), record.details());
            }
        });
    }

    private Class<?> getMethod(EventSourceEntity entity, Class<?> event) throws NoSuchMethodException {
        return eventTypesExtractor.extractTypes(entity).stream()
                .filter(klass -> klass.isAssignableFrom(event))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException("No such method: public void apply(" + event.getName()
                        + ") on " + entity.getClass().getName()));
    }
}
