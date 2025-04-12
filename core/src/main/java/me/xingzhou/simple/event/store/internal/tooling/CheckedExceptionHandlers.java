package me.xingzhou.simple.event.store.internal.tooling;

/** Make dealing with checked exceptions a bit easier. */
public interface CheckedExceptionHandlers {
    /**
     * Handle potential exceptions thrown from fn.
     *
     * @param fn the function that may throw an exception
     */
    static void handleExceptions(ThrowableRunnable fn) {
        try {
            fn.execute();
        } catch (Throwable e) {
            throw wrapAsRuntime(e);
        }
    }

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
        } catch (Throwable e) {
            throw wrapAsRuntime(e);
        }
    }

    private static RuntimeException wrapAsRuntime(Throwable e) {
        if (e instanceof RuntimeException re) {
            return re;
        }
        return new RuntimeException(e);
    }

    @FunctionalInterface
    interface ThrowableRunnable {
        void execute() throws Throwable;
    }

    @FunctionalInterface
    interface ThrowableSupplier<R> {
        R execute() throws Throwable;
    }
}
