package me.xingzhou.projects.simple.event.store;

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

  public AppendToken createStream(StreamName streamName, Event event) {
    var serializedEvent = serializer.serialize(event);
    var token =
        storage.createStream(
            streamName.value(),
            event.id(),
            serializedEvent.eventName(),
            serializedEvent.eventJson());
    return new AppendToken(token);
  }
}
