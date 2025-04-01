package me.xingzhou.projects.simple.event.store.serializer.adapters;

public class DeserializationFailure extends RuntimeException {
    public DeserializationFailure(String eventType) {
        super(String.format("""
                Unable to deserialize "%s" to a known type.""", eventType));
    }
}
