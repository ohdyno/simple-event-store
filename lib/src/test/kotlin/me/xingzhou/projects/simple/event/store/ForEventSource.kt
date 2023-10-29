package me.xingzhou.projects.simple.event.store

interface ForEventSource {
  fun createStream(streamName: StreamName, event: DomainEvent)

  fun retrieveEvents(streamName: StreamName): List<DomainEvent>

  fun appendToStream(streamName: StreamName, event: DomainEvent, appendToken: AppendToken)
}
