package me.xingzhou.projects.simple.event.store.storage.failures;

public class NoSuchStreamFailure extends RuntimeException {
    public NoSuchStreamFailure(String streamName) {
        super("Stream '" + streamName + "' does not exist");
    }
}
