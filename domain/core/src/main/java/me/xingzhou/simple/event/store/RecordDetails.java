package me.xingzhou.simple.event.store;

import java.time.Instant;
import me.xingzhou.simple.event.store.ids.RecordId;
import me.xingzhou.simple.event.store.ids.StreamName;
import me.xingzhou.simple.event.store.ids.Version;

public record RecordDetails(StreamName streamName, Version version, RecordId recordId, Instant insertedOn) {
    public RecordDetails(String streamName, long version, long id, Instant insertedOn) {
        this(new StreamName(streamName), new Version(version), new RecordId(id), insertedOn);
    }
}
