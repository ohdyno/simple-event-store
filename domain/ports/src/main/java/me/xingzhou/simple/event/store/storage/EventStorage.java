package me.xingzhou.simple.event.store.storage;

import jakarta.annotation.Nonnull;
import java.time.Instant;
import java.util.Collection;

/**
 * Primary abstraction for event storage operations in the event store. Provides methods to append events to streams and
 * retrieve events from streams.
 */
public interface EventStorage {
    /**
     * Appends a new event to the specified stream.
     *
     * @param streamName the name of the stream to append to
     * @param currentVersion the current version of the stream for optimistic concurrency control
     * @param eventType the type of event being appended
     * @param eventContent the serialized content of the event
     * @return the stored record containing the persisted event details
     */
    @Nonnull
    StoredRecord appendEvent(
            @Nonnull String streamName, long currentVersion, @Nonnull String eventType, @Nonnull String eventContent);

    /**
     * Supports the following most common scenarios: - Given a version, retrieve all events after this version. - Given
     * a version, retrieve all events led to this version.
     */
    /**
     * Retrieves events from a specific stream within a version range.
     *
     * @param streamName the name of the stream to retrieve from
     * @param eventTypes the types of events to include in the results
     * @param exclusiveStartVersion events after this version will be included (exclusive)
     * @param inclusiveEndVersion events up to and including this version will be included (inclusive)
     * @return the retrieved records and the latest record information
     */
    @Nonnull
    RetrievedRecords retrieveEvents(
            @Nonnull String streamName,
            @Nonnull Collection<String> eventTypes,
            long exclusiveStartVersion,
            long inclusiveEndVersion);

    /**
     * Retrieves events across multiple streams within an ID range.
     *
     * @param exclusiveStartId events after this ID will be included (exclusive)
     * @param inclusiveEndId events up to and including this ID will be included (inclusive)
     * @param streamNames the names of streams to retrieve from
     * @param eventTypes the types of events to include in the results
     * @return the retrieved records and the latest record information
     */
    @Nonnull
    RetrievedRecords retrieveEvents(
            long exclusiveStartId,
            long inclusiveEndId,
            @Nonnull Collection<String> streamNames,
            @Nonnull Collection<String> eventTypes);

    /** Constants used throughout the event storage system. */
    interface Constants {
        /** Constants for event record IDs. */
        interface Ids {
            /** Minimum ID value and undefined ID constant. */
            long MIN = 0L, UNDEFINED = MIN;
            /** Starting ID for the first event. */
            long START = 1L;
            /** Maximum possible ID value. */
            long MAX = Long.MAX_VALUE;
        }

        /** Constants for event insertion timestamps. */
        interface InsertedOnTimestamps {
            /** Timestamp representing 'never inserted' or undefined timestamp. */
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
            /** Minimum version value and constant for undefined streams. */
            long MIN = -1, UNDEFINED_STREAM = MIN;
            /** Maximum possible version value. */
            long MAX = Long.MAX_VALUE;
            /** Version number for new streams. */
            long NEW_STREAM = 0;
        }
    }
}
