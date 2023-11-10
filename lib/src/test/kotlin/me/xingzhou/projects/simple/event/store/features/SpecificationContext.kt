package me.xingzhou.projects.simple.event.store.features

import me.xingzhou.projects.simple.event.store.*
import me.xingzhou.projects.simple.event.store.result.EventStoreResult

class SpecificationContext {
  var result: Result<EventStoreResult>? = null
  var streamName: StreamName? = null
  var occurredOn: OccurredOn? = null
  var event: DomainEvent? = null
  lateinit var store: EventStore
  lateinit var adapter: ForEventSource
}
