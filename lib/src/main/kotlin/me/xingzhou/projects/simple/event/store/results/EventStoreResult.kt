package me.xingzhou.projects.simple.event.store.results

import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.StreamName

sealed interface EventStoreResult {
  data class ForCreateStream(val appendToken: AppendToken) : EventStoreResult

  data class ForRetrieveFromStream(val events: List<RetrievedEvent>) : EventStoreResult

  data class ForCheckStreamExists(val result: Boolean) : EventStoreResult

  sealed class Failure(val message: String) : EventStoreResult {
    class StreamAlreadyExists(val streamName: StreamName, message: String) : Failure(message)
  }
}
