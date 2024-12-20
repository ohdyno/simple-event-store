package me.xingzhou.projects.simple.event.store

import me.xingzhou.projects.simple.event.store.result.EventStoreResult

class EventStore(
    private val adapterForEventSource: ForEventSource,
    private val serializer: EventSerializer
) {
  fun createStream(streamName: StreamName, event: Event, occurredOn: OccurredOn): EventStoreResult {
    val appendToken =
        adapterForEventSource.createStream(
            streamName.name, serializer.serialize(event), occurredOn.instant)
    return EventStoreResult.ForCreateStream(AppendToken(appendToken))
  }

  fun retrieveFromStream(streamName: StreamName): EventStoreResult {
    var events = adapterForEventSource.retrieveFromStream(streamName.name)
    return EventStoreResult.ForRetrieveFromStream(events)
  }
}

interface EventSerializer {
  fun serialize(event: Event): ByteArray
}
