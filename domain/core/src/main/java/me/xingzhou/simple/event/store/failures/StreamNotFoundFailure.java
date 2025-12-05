package me.xingzhou.simple.event.store.failures;

import me.xingzhou.simple.event.store.storage.failures.NoSuchStreamFailure;

public class StreamNotFoundFailure extends RuntimeException {
    public StreamNotFoundFailure(NoSuchStreamFailure e) {
        super("Stream '%s' not found".formatted(e.streamName()), e);
    }
}
