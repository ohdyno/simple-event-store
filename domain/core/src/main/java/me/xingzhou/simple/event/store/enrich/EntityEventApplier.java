package me.xingzhou.simple.event.store.enrich;

import static me.xingzhou.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import me.xingzhou.simple.event.store.entities.EventSourceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityEventApplier {

    private static final Logger log = LoggerFactory.getLogger(EntityEventApplier.class);
    private final EventTypesExtractor eventTypesExtractor;

    public EntityEventApplier(EventTypesExtractor eventTypesExtractor) {
        this.eventTypesExtractor = eventTypesExtractor;
    }

    public <T extends EventSourceEntity> void apply(EventRecord record, T entity) {
        log.debug("apply {} to entity {}", record, entity);
        handleExceptions(() -> {
            var parameterType = getMethod(entity, record.event().getClass());
            try {
                log.debug("attempting to find method {}({})", EventSourceEntity.APPLY_METHOD_NAME, parameterType);
                var method = entity.getClass().getMethod(EventSourceEntity.APPLY_METHOD_NAME, parameterType);

                log.debug("attempting to invoke method {}({})", EventSourceEntity.APPLY_METHOD_NAME, parameterType);
                method.invoke(entity, record.event());
            } catch (NoSuchMethodException e) {
                log.debug("unable to find method {}({})", EventSourceEntity.APPLY_METHOD_NAME, parameterType);

                log.debug(
                        "attempting to find public method {}({}, {})",
                        EventSourceEntity.APPLY_METHOD_NAME,
                        parameterType,
                        record.details().getClass());
                var method = entity.getClass()
                        .getMethod(
                                EventSourceEntity.APPLY_METHOD_NAME,
                                parameterType,
                                record.details().getClass());

                log.debug(
                        "attempting to invoke method {}({}, {})",
                        EventSourceEntity.APPLY_METHOD_NAME,
                        parameterType,
                        record.details().getClass());
                method.invoke(entity, record.event(), record.details());
            }
        });
    }

    private Class<?> getMethod(EventSourceEntity entity, Class<?> event) throws NoSuchMethodException {
        log.debug(
                "attempting to find the first parameter of the first {}(<? extends {}>) method",
                EventSourceEntity.APPLY_METHOD_NAME,
                event);
        return eventTypesExtractor.extractTypes(entity).stream()
                .filter(klass -> klass.isAssignableFrom(event))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException(
                        "No such method: public void " + EventSourceEntity.APPLY_METHOD_NAME + "(" + event.getName()
                                + ") on " + entity.getClass().getName()));
    }
}
