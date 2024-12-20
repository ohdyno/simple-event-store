package me.xingzhou.projects.simple.event.store.features

import me.xingzhou.projects.simple.event.store.*
import me.xingzhou.projects.simple.event.store.result.EventStoreResult

class SpecificationContext {
  lateinit var result: EventStoreResult
  lateinit var streamName: StreamName
  lateinit var occurredOn: OccurredOn
  lateinit var event: Event
  lateinit var store: EventStore
  lateinit var adapter: ForEventSource
  lateinit var serializer: EventSerializer
}
