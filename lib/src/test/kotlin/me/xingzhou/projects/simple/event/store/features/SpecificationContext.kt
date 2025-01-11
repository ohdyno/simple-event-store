package me.xingzhou.projects.simple.event.store.features

import kotlin.reflect.KClass
import me.xingzhou.projects.simple.event.store.*
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromSystem
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage
import me.xingzhou.projects.simple.event.store.results.EventStoreResult
import me.xingzhou.projects.simple.event.store.results.RetrievedEvent

class SpecificationContext {
  lateinit var eventStorageSnapshot: EventStoreResult.ForRetrieveFromSystem
  lateinit var result: EventStoreResult
  lateinit var streamName: StreamName
  lateinit var appendToken: AppendToken
  lateinit var occurredOn: OccurredOn
  lateinit var event: Event
  lateinit var eventStorage: ForEventStorage
  lateinit var eventSerializer: ForEventSerializer
  val expectedStorageContent = mutableMapOf<StreamName, List<RetrievedEvent>>()
  val desiredEventTypes = mutableListOf<KClass<out Event>>()
}

fun SpecificationContext.snapshotEventStorage(): EventStoreResult.ForRetrieveFromSystem {
  return with(
      ExecutionContext(
          command = RetrieveFromSystem(),
          forEventStorage = eventStorage,
          forEventSerialization = eventSerializer)) {
        EventStore().handle(this) as EventStoreResult.ForRetrieveFromSystem
      }
}

fun SpecificationContext.store(streamName: StreamName, events: List<RetrievedEvent>) {
  expectedStorageContent[streamName] = events
}
