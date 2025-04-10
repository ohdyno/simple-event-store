package me.xingzhou.projects.simple.event.store;

import java.time.Instant;

public record RecordDetails(StreamName streamName, Version version, Record record, Instant insertedOn) {
    public RecordDetails(String streamName, long version, long id, Instant insertedOn) {
        this(new StreamName(streamName), new Version(version), new Record(id), insertedOn);
    }
}
