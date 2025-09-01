package me.xingzhou.simple.event.store.storage.failures;

/**
 * Exception thrown when attempting to append an event with a stale version. This indicates an optimistic concurrency
 * control violation.
 */
public class StaleVersionFailure extends RuntimeException {
    /** Constructs a new StaleVersionFailure. */
    public StaleVersionFailure() {
        super();
    }
}
