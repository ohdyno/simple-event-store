package me.xingzhou.projects.simple.event.store;

import java.time.Instant;

public record RecordDetails(StreamName streamName, Version version, EventId eventId, Instant timestamp) {
    public RecordDetails(String streamName, long version, long eventId, Instant timestamp) {
        this(new StreamName(streamName), new Version(version), new EventId(eventId), timestamp);
    }
}
