package me.xingzhou.projects.simple.event.store.storage;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import me.xingzhou.projects.simple.event.store.Version;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;

public interface EventStorage {
    /**
     * Create an event stream with the given streamName containing the event defined by (eventId, eventType,
     * eventContent).
     *
     * @param streamName is the name of the new stream. Streams cannot have duplicate names.
     * @param eventId is the id associated with this event. Duplicate ids are allowed.
     * @param eventType is the type of this event. The type can be passed as part of the eventTypes parameter in
     *     {@link #retrieveEvents(String, List, long, long)} or {@link #retrieveEvents(String, List, Instant, Instant)}
     *     to reduce the number of events retrieved.
     * @param eventContent is the content of the event serialized to JSON.
     * @return a version reflecting the state of the event stream.
     * @throws DuplicateEventStreamFailure if another stream with the same name already exists.
     * @see Version
     */
    long createStream(
            @Nonnull String streamName,
            @Nonnull String eventId,
            @Nonnull String eventType,
            @Nonnull String eventContent);

    long appendEvent(String streamName, long currentVersion, String eventId, String eventType, String eventContent);

    VersionedRecords retrieveEvents(String streamName, List<String> eventTypes, long beginVersion, long endVersion);

    default TimestampedRecords retrieveEvents(String streamName, List<String> eventTypes, Instant start, Instant end) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * @return the version indicating a new event stream. This is the same as the value returned from
     *     {@link #createStream(String, String, String, String)}. The value is guaranteed to be between
     *     {@link #undefinedVersion()} and {@link #exclusiveMaxVersion()}.
     * @apiNote The possible value returned is an internal detail of this class. Interpreting and manipulating the value
     *     is unsupported and could result in undefined behavior.
     */
    long newStreamVersion();

    /**
     * @return the exclusive lower bound for the version.
     * @apiNote The specific value is an internal detail of this class. It could be changed in unexpected ways.
     */
    default long undefinedVersion() {
        return -1;
    }

    /**
     * @return the exclusive upper bound for the version.
     * @apiNote The specific value is an internal detail of this class. It could be changed in unexpected ways.
     */
    default long exclusiveMaxVersion() {
        return Long.MAX_VALUE;
    }
}
