package me.xingzhou.projects.simple.event.store.results

import me.xingzhou.projects.simple.event.store.AppendToken

sealed class EventStoreResult {
  data class ForCreateStream(val appendToken: AppendToken) : EventStoreResult()

  data class ForRetrieveFromStream(val events: List<RetrievedEvent>) : EventStoreResult()
}
