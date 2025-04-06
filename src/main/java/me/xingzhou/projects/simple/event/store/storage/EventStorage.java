package me.xingzhou.projects.simple.event.store.storage;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.List;

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
        long RANGE_MAX_INCLUSIVE = Long.MAX_VALUE;
    }

    interface TimestampConstants {
        Instant NEVER = Instant.EPOCH;
    }

    StoredRecord appendEvent(
            @Nonnull String streamName,
            long currentVersion,
            @Nonnull String eventId,
            @Nonnull String eventType,
            @Nonnull String eventContent);

    /**
     * Supports the following most common scenarios: - Given a version, retrieve all events after this version. - Given
     * a version, retrieve all events led to this version.
     */
    @Nonnull
    VersionedRecords retrieveEvents(
            @Nonnull String streamName,
            @Nonnull List<String> eventTypes,
            long exclusiveStartVersion,
            long inclusiveEndVersion);

    @Nonnull
    TimestampedRecords retrieveEvents(
            @Nonnull Instant exclusiveStart,
            @Nonnull Instant exclusiveEnd,
            @Nonnull List<String> streamNames,
            @Nonnull List<String> eventTypes);
}
