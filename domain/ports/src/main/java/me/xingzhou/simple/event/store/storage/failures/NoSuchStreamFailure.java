package me.xingzhou.simple.event.store.storage.failures;

/** Exception thrown when attempting to access a stream that does not exist. */
public class NoSuchStreamFailure extends RuntimeException {
    /**
     * Constructs a new NoSuchStreamFailure with the specified stream name.
     *
     * @param streamName the name of the stream that does not exist
     */
    public NoSuchStreamFailure(String streamName) {
        super("Stream '" + streamName + "' does not exist");
    }
}
