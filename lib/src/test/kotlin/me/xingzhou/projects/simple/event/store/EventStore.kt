package me.xingzhou.projects.simple.event.store

import me.xingzhou.projects.simple.event.store.result.EventStoreResult

class EventStore(private val adapterForEventSource: ForEventSource) {
  fun createStream(
      streamName: StreamName,
      event: DomainEvent,
      occurredOn: OccurredOn
  ): Result<EventStoreResult> {
    return Result.success(EventStoreResult.ForCreateStream(AppendToken("0")))
  }

  fun retrieveFromStream(streamName: StreamName): Result<EventStoreResult> {
    return Result.success(EventStoreResult.ForRetrieveFromStream(listOf()))
  }
}
