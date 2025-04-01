package me.xingzhou.projects.simple.event.store.internal.tooling;

/** Make dealing with checked exceptions a bit easier. */
public interface CheckedExceptionHandlers {
    /**
     * Handle potential exceptions thrown from fn.
     *
     * @param fn the function that may throw an exception
     * @return the value returned from fn
     * @param <R> the type of value returned from fn
     */
    static <R> R handleExceptions(ThrowableSupplier<R> fn) {
        try {
            return fn.execute();
        } catch (Exception e) {
            if (e instanceof RuntimeException re) {
                throw re;
            }
            throw new RuntimeException(e);
        }
    }

    @FunctionalInterface
    interface ThrowableSupplier<R> {
        R execute() throws Exception;
    }
}
