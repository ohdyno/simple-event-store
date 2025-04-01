package me.xingzhou.projects.simple.event.store;

import java.util.Collections;
import java.util.List;

public class EventStore {
  private final EventStorage storage;
  private final EventSerializer serializer;

  private EventStore(EventStorage storage, EventSerializer serializer) {
    this.storage = storage;
    this.serializer = serializer;
  }

  public static EventStore build(EventStorage storage, EventSerializer serializer) {
    return new EventStore(storage, serializer);
  }

  public Version createStream(StreamName streamName, Event event) {
    var serializedEvent = serializer.serialize(event);
    var current =
        storage.createStream(
            streamName.value(),
            event.id(),
            serializedEvent.eventType(),
            serializedEvent.eventJson());
    return new Version(current);
  }

  public Version appendEvent(StreamName streamName, Event event, Version current) {
    var serializedEvent = serializer.serialize(event);
    var next =
        storage.appendEvent(
            streamName.value(),
            current.value(),
            event.id(),
            serializedEvent.eventType(),
            serializedEvent.eventJson());
    return new Version(next);
  }

  public VersionedEvents retrieveEvents(StreamName streamName) {
    return retrieveEvents(streamName, Collections.emptyList(), Version.start(), Version.end());
  }

  public VersionedEvents retrieveEventsStartingAfter(StreamName streamName, Version version) {
    return retrieveEvents(streamName, Collections.emptyList(), version, Version.end());
  }

  public VersionedEvents retrieveEventsUpToExclusive(StreamName streamName, Version version) {
    return retrieveEvents(streamName, Collections.emptyList(), Version.start(), version);
  }

  private VersionedEvents retrieveEvents(
      StreamName streamName, List<Class<? extends Event>> eventTypes, Version start, Version end) {
    var typeNames = eventTypes.stream().map(serializer::getTypeName).toList();
    var versionedRecords =
        storage.retrieveEvents(streamName.value(), typeNames, start.value(), end.value());
    var events =
        versionedRecords.records().stream()
            .map(record -> VersionedEvent.from(record, serializer))
            .toList();
    return new VersionedEvents(events, new Version(versionedRecords.version()));
  }
}
