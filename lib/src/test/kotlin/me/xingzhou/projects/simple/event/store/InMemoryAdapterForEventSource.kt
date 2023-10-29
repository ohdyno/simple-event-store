package me.xingzhou.projects.simple.event.store

class InMemoryAdapterForEventSource : ForEventSource {
  private val streams = mutableMapOf<StreamName, MutableList<DomainEvent>>()

  override fun createStream(streamName: StreamName, event: DomainEvent) {
    streams[streamName] = mutableListOf(event)
  }

  override fun retrieveEvents(streamName: StreamName): List<DomainEvent> {
    return streams[streamName]!!
  }
}
