package me.xingzhou.projects.simple.event.store;

import java.time.Instant;
import java.util.List;

public interface EventStorage {
    String createStream(String streamName, String eventId, String eventType, String eventJson);

    String appendEvent(String streamName, String appendToken, String eventId, String eventType, String eventJson);

    VersionedRecords retrieveEvents(String streamName, List<String> eventTypes, String begin, String end);

    TimestampedRecords retrieveEvents(String streamName, List<String> eventTypes, Instant start, Instant end);

    record VersionedRecords(List<StoredRecord> records, String version) {}

    record TimestampedRecords(List<StoredRecord> records, Instant timestamp) {}

    record StoredRecord(String eventType, String eventJson, String streamName, String version) {}
}
