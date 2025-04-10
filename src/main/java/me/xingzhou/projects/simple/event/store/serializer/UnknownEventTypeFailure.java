package me.xingzhou.projects.simple.event.store.serializer;

public class UnknownEventTypeFailure extends RuntimeException {
    public UnknownEventTypeFailure(Class<?> klass) {
        super(String.format("""
				Unknown event type: '%s'""", klass.getName()));
    }

    public UnknownEventTypeFailure(String eventType) {
        super(String.format("""
				Unknown event type: '%s'""", eventType));
    }
}
