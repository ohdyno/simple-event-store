package me.xingzhou.simple.event.store.event.converter;

/**
 * Converts between event classes and their string type representations. This interface provides bidirectional
 * conversion capabilities for event type mapping.
 */
public interface EventTypeConverter {
    /**
     * Converts an event class to its string type representation.
     *
     * @param event the event class to convert
     * @return the string representation of the event type
     */
    String convert(Class<?> event);

    /**
     * Converts a string event type to its corresponding class.
     *
     * @param eventType the string representation of the event type
     * @return the class corresponding to the event type
     */
    Class<?> convert(String eventType);
}
