package me.xingzhou.projects.simple.event.store.storage;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import me.xingzhou.projects.simple.event.store.Version;

public interface EventStorage {
    /**
     * Create an event stream with the given streamName containing the event defined by (eventId, eventType,
     * eventContent).
     *
     * @param streamName is the name of the new stream. Streams cannot have duplicate names.
     * @throws DuplicateEventStreamFailure if another stream with the same name already exists.
     * @param eventId is the id associated with this event. Duplicate ids are allowed.
     * @param eventType is the type of this event. The type can be passed as part of the eventTypes parameter in
     *     {@link #retrieveEvents(String, List, String, String)} or {@link #retrieveEvents(String, List, Instant,
     *     Instant)} to reduce the number of events retrieved.
     * @param eventContent is the content of the event serialized to JSON.
     * @return a version reflecting the state of the event stream.
     * @see Version
     */
    @Nonnull
    String createStream(
            @Nonnull String streamName,
            @Nonnull String eventId,
            @Nonnull String eventType,
            @Nonnull String eventContent);

    default String appendEvent(
            String streamName, String appendToken, String eventId, String eventType, String eventContent) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    default VersionedRecords retrieveEvents(String streamName, List<String> eventTypes, String begin, String end) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    default TimestampedRecords retrieveEvents(String streamName, List<String> eventTypes, Instant start, Instant end) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @return the initial version. The initial version is the same as the value returned from
     *     {@link #createStream(String, String, String, String)}.
     * @apiNote The content with the return value should be treated as an implementation detail specific to this class.
     *     Therefore, parsing the content and interpreting its value is unsupported and could break without warning.
     */
    String initialVersion();
}
