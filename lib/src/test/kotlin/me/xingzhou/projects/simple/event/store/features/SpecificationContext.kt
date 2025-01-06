package me.xingzhou.projects.simple.event.store.features

import me.xingzhou.projects.simple.event.store.*
import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.StreamEvent
import me.xingzhou.projects.simple.event.store.results.EventStoreResult

class SpecificationContext {
  fun snapshotEventStorage(): Map<String, List<StreamEvent>> {
    return eventStorage.retrieveFromSystem().fold(emptyMap()) { map, systemEvent ->
      map.getOrDefault(systemEvent.streamName, emptyList()).let { streamEvents ->
        (streamEvents + systemEvent.streamEvent).let { map + (systemEvent.streamName to it) }
      }
    }
  }

  lateinit var eventStorageSnapshot: Map<String, List<StreamEvent>>
  lateinit var result: EventStoreResult
  lateinit var streamName: StreamName
  lateinit var appendToken: AppendToken
  lateinit var occurredOn: OccurredOn
  lateinit var event: Event
  lateinit var store: EventStore
  lateinit var eventStorage: ForEventStorage
  lateinit var eventSerializer: ForEventSerializer
}
