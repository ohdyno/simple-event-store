package me.xingzhou.simple.event.store.storage;

import java.time.Instant;
import me.xingzhou.simple.event.store.storage.EventStorage.Constants.Ids;
import me.xingzhou.simple.event.store.storage.EventStorage.Constants.InsertedOnTimestamps;
import me.xingzhou.simple.event.store.storage.EventStorage.Constants.Versions;

/**
 * Represents a stored event record with all its metadata.
 *
 * @param id the unique identifier of the stored record
 * @param streamName the name of the stream this record belongs to
 * @param eventType the type of the stored event
 * @param eventContent the serialized content of the event
 * @param version the version number of this event within its stream
 * @param insertedOn the timestamp when this record was inserted
 */
public record StoredRecord(
        long id, String streamName, String eventType, String eventContent, long version, Instant insertedOn) {
    /**
     * Creates an empty record with default/undefined values.
     *
     * @return an empty StoredRecord instance
     */
    public static StoredRecord emptyRecord() {
        return new StoredRecord(Ids.UNDEFINED, "", "", "", Versions.UNDEFINED_STREAM, InsertedOnTimestamps.NEVER);
    }
}
