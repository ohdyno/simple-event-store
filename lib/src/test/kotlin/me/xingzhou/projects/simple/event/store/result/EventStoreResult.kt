package me.xingzhou.projects.simple.event.store.result

import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.RetrievedEvent

sealed class EventStoreResult {
  data class ForCreateStream(val appendToken: AppendToken) : EventStoreResult()

  data class ForRetrieveFromStream(val events: List<RetrievedEvent>) : EventStoreResult()
}
