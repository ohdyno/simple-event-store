package me.xingzhou.simple.event.store.enrich;

import static me.xingzhou.simple.event.store.entities.EventSourceEntity.APPLY_METHOD_NAME;
import static me.xingzhou.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
import me.xingzhou.simple.event.store.entities.EventSourceEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityEventApplier {

    private static final Logger log = LoggerFactory.getLogger(EntityEventApplier.class);

    private static <T extends EventSourceEntity> void applyWithEvent(
            EventRecord record, T entity, Class<?> parameterType) throws InvocationTargetException {
        try {
            log.debug("attempting to find accessible method {}({})", APPLY_METHOD_NAME, parameterType);
            var method = entity.getClass().getMethod(APPLY_METHOD_NAME, parameterType);

            log.debug("attempting to invoke method {}({})", APPLY_METHOD_NAME, parameterType);
            method.invoke(entity, record.event());
        } catch (NoSuchMethodException | IllegalAccessException e) {
            log.debug(
                    "unable to apply event because unable to find accessible method {}({})",
                    APPLY_METHOD_NAME,
                    parameterType);
            applyWithEventAndRecordDetails(record, entity, parameterType);
        }
    }

    private static <T extends EventSourceEntity> void applyWithEventAndRecordDetails(
            EventRecord record, T entity, Class<?> parameterType) throws InvocationTargetException {
        try {
            log.debug(
                    "attempting to find accessible method {}({}, {})",
                    APPLY_METHOD_NAME,
                    parameterType,
                    record.details().getClass());
            var method = entity.getClass()
                    .getMethod(
                            APPLY_METHOD_NAME, parameterType, record.details().getClass());

            log.debug(
                    "attempting to invoke method {}({}, {})",
                    APPLY_METHOD_NAME,
                    parameterType,
                    record.details().getClass());
            method.invoke(entity, record.event(), record.details());
        } catch (NoSuchMethodException | IllegalAccessException ex) {
            log.debug(
                    "unable to apply event because unable to find accessible method {}({}, {})",
                    APPLY_METHOD_NAME,
                    parameterType,
                    record.details().getClass());
        }
    }

    private final EventTypesExtractor eventTypesExtractor;

    public EntityEventApplier(EventTypesExtractor eventTypesExtractor) {
        this.eventTypesExtractor = eventTypesExtractor;
    }

    public <T extends EventSourceEntity> void apply(EventRecord record, T entity) {
        log.debug("apply {} to entity {}", record, entity);
        handleExceptions(() -> {
            var parameterTypeForApplyMethod = getMethod(entity, record.event().getClass());
            if (parameterTypeForApplyMethod.isPresent()) {
                var parameterType = parameterTypeForApplyMethod.get();
                applyWithEvent(record, entity, parameterType);
            }
        });
    }

    private Optional<Class<?>> getMethod(EventSourceEntity entity, Class<?> event) {
        log.debug(
                "attempting to find the first parameter of the first {}(<? extends {}>) method",
                APPLY_METHOD_NAME,
                event);
        return eventTypesExtractor.extractTypes(entity).stream()
                .filter(klass -> klass.isAssignableFrom(event))
                .findFirst()
                .or(() -> {
                    log.debug(
                            "unable to find the first parameter of the first {}(<? extends {}>) method",
                            APPLY_METHOD_NAME,
                            event);
                    return Optional.empty();
                });
    }
}
