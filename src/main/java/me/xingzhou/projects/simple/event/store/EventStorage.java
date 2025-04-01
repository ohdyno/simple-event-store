package me.xingzhou.projects.simple.event.store;

import java.util.List;

public interface EventStorage {
  String createStream(String streamName, String eventId, String eventType, String eventJson);

  String appendEvent(
      String streamName, String appendToken, String eventId, String eventType, String eventJson);

  VersionedRecords retrieveEvents(
      String streamName, List<String> eventTypes, String begin, String end);

  record VersionedRecords(
      List<EventStorage.VersionedRecords.VersionedRecord> records, String version) {
    public record VersionedRecord() {
      public String eventType() {
        return null;
      }

      public String eventJson() {
        return null;
      }

      public String streamName() {
        return null;
      }

      public String version() {
        return null;
      }
    }
  }
}
