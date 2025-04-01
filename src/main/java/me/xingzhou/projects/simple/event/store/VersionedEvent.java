package me.xingzhou.projects.simple.event.store;

import me.xingzhou.projects.simple.event.store.EventStorage.VersionedRecords.VersionedRecord;

public record VersionedEvent(StreamName streamName, Event event, Version version) {
  public static VersionedEvent from(VersionedRecord record, EventSerializer serializer) {
    var event = serializer.deserialize(record.eventType(), record.eventJson());
    return new VersionedEvent(
        new StreamName(record.streamName()), event, new Version(record.version()));
  }
}
