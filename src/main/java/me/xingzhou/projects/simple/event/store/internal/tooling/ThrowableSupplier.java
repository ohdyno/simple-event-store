package me.xingzhou.projects.simple.event.store.internal.tooling;

@FunctionalInterface
public interface ThrowableSupplier<R> {
    R execute() throws Exception;
}
