package me.xingzhou.simple.event.store.entities;

public interface EventSourceEntity {
    String APPLY_METHOD_NAME = "apply";

    default boolean isDefined() {
        return false;
    }
}
