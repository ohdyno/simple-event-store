package me.xingzhou.simple.event.store.storage.failures;

/** Exception thrown when attempting to create a stream that already exists. */
public class DuplicateEventStreamFailure extends RuntimeException {
    /** Constructs a new DuplicateEventStreamFailure. */
    public DuplicateEventStreamFailure() {
        super();
    }
}
