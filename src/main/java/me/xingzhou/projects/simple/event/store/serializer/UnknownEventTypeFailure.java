package me.xingzhou.projects.simple.event.store.serializer;

public class UnknownEventTypeFailure extends RuntimeException {
    public UnknownEventTypeFailure(String eventType) {
        super(String.format("""
				Unknown event type: '%s'""", eventType));
    }
}
