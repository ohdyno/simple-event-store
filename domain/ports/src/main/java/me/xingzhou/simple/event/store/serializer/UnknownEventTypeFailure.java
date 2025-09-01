package me.xingzhou.simple.event.store.serializer;

/** Exception thrown when an unknown event type is encountered during serialization or deserialization. */
public class UnknownEventTypeFailure extends RuntimeException {
    /**
     * Constructs a new UnknownEventTypeFailure with the specified event class.
     *
     * @param klass the unknown event class
     */
    public UnknownEventTypeFailure(Class<?> klass) {
        super(String.format("""
				Unknown event type: '%s'""", klass.getName()));
    }

    /**
     * Constructs a new UnknownEventTypeFailure with the specified event type string.
     *
     * @param eventType the unknown event type string
     */
    public UnknownEventTypeFailure(String eventType) {
        super(String.format("""
				Unknown event type: '%s'""", eventType));
    }
}
