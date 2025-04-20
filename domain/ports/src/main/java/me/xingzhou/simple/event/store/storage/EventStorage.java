package me.xingzhou.simple.event.store.storage;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.Collection;

public interface EventStorage {
    @Nonnull
    StoredRecord appendEvent(
            @Nonnull String streamName, long currentVersion, @Nonnull String eventType, @Nonnull String eventContent);

    /**
     * Supports the following most common scenarios: - Given a version, retrieve all events after this version. - Given
     * a version, retrieve all events led to this version.
     */
    @Nonnull
    RetrievedRecords retrieveEvents(
            @Nonnull String streamName,
            @Nonnull Collection<String> eventTypes,
            long exclusiveStartVersion,
            long inclusiveEndVersion);

    @Nonnull
    RetrievedRecords retrieveEvents(
            long exclusiveStartId,
            long inclusiveEndId,
            @Nonnull Collection<String> streamNames,
            @Nonnull Collection<String> eventTypes);

    interface Constants {
        interface Ids {
            long MIN = 0L, UNDEFINED = MIN;
            long START = 1L;
            long MAX = Long.MAX_VALUE;
        }

        interface InsertedOnTimestamps {
            Instant NEVER = Instant.EPOCH;
        }

        /**
         * Define contractual version constants.
         *
         * @apiNote The values can be considered stable across major releases. Therefore, the version can be safely
         *     persisted and read regardless of the release.
         * @implSpec Since the other methods rely on the value of version, all implementations should use these
         *     constants when appropriate.
         */
        interface Versions {
            long MIN = -1, UNDEFINED_STREAM = MIN;
            long MAX = Long.MAX_VALUE;
            long NEW_STREAM = 0;
        }
    }
}
