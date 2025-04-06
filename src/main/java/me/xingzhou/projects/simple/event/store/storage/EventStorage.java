package me.xingzhou.projects.simple.event.store.storage;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import me.xingzhou.projects.simple.event.store.Version;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;

public interface EventStorage {
    /**
     * Define contractual version constants.
     *
     * @apiNote The values can be considered stable across major releases. Therefore, the version can be safely
     *     persisted and read regardless of the release.
     * @implSpec Since the other methods rely on the value of version, all implementations should use these constants
     *     when appropriate.
     */
    interface VersionConstants {
        long NEW_STREAM = 0;
        long UNDEFINED_STREAM = -1;
        long RANGE_MIN_EXCLUSIVE = UNDEFINED_STREAM;
        long RANGE_MAX_EXCLUSIVE = Long.MAX_VALUE;
    }

    /**
     * Create an event stream with the given streamName containing the event defined by (eventId, eventType,
     * eventContent).
     *
     * @param streamName is the name of the new stream. Streams cannot have duplicate names.
     * @param eventId is the id associated with this event. Duplicate ids are allowed.
     * @param eventType is the type of this event. The type can be passed as part of the eventTypes parameter in
     *     {@link #retrieveEvents(String, List, long, long)} or {@link #retrieveEvents(Instant, Instant, List, List)} to
     *     reduce the number of events retrieved.
     * @param eventContent is the content of the event serialized to JSON.
     * @return {@link VersionConstants#NEW_STREAM}
     * @throws DuplicateEventStreamFailure if another stream with the same name already exists.
     * @see Version
     */
    long createStream(
            @Nonnull String streamName,
            @Nonnull String eventId,
            @Nonnull String eventType,
            @Nonnull String eventContent);

    long appendEvent(
            @Nonnull String streamName,
            long currentVersion,
            @Nonnull String eventId,
            @Nonnull String eventType,
            @Nonnull String eventContent);

    @Nonnull
    VersionedRecords retrieveEvents(
            @Nonnull String streamName, @Nonnull List<String> eventTypes, long beginVersion, long endVersion);

    @Nonnull
    TimestampedRecords retrieveEvents(
            @Nonnull Instant exclusiveStart,
            @Nonnull Instant exclusiveEnd,
            @Nonnull List<String> streamNames,
            @Nonnull List<String> eventTypes);
}
