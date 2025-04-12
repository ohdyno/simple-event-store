package me.xingzhou.simple.event.store;

import java.time.Instant;

public record RecordDetails(StreamName streamName, Version version, RecordId recordId, Instant insertedOn) {
    public RecordDetails(String streamName, long version, long id, Instant insertedOn) {
        this(new StreamName(streamName), new Version(version), new RecordId(id), insertedOn);
    }
}
