package me.xingzhou.simple.event.store.storage;

import java.time.Instant;
import me.xingzhou.simple.event.store.storage.EventStorage.Constants.Ids;
import me.xingzhou.simple.event.store.storage.EventStorage.Constants.InsertedOnTimestamps;
import me.xingzhou.simple.event.store.storage.EventStorage.Constants.Versions;

public record StoredRecord(
        long id, String streamName, String eventType, String eventContent, long version, Instant insertedOn) {
    public static StoredRecord emptyRecord() {
        return new StoredRecord(Ids.UNDEFINED, "", "", "", Versions.UNDEFINED_STREAM, InsertedOnTimestamps.NEVER);
    }
}
