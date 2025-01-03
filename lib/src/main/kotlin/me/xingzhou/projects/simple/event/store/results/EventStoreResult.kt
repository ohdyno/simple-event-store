package me.xingzhou.projects.simple.event.store.results

import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.StreamName

sealed interface EventStoreResult {
  sealed class Failure(val message: String) : EventStoreResult {
    class StreamAlreadyExists(val streamName: StreamName, message: String) : Failure(message)

    class StreamDoesNotExist(val streamName: StreamName, message: String) : Failure(message)

    class InvalidAppendToken(
        val streamName: StreamName,
        val appendToken: AppendToken,
        message: String
    ) : Failure(message)
  }

  data class ForCreateStream(val appendToken: AppendToken) : EventStoreResult

  data class ForRetrieveFromStream(val events: List<RetrievedEvent>) : EventStoreResult

  data class ForCheckStreamExists(val result: Boolean) : EventStoreResult

  data class ForValidateAppendToken(val result: Boolean) : EventStoreResult

  data class ForRetrieveAppendToken(val appendToken: AppendToken) : EventStoreResult

  data class ForAppendToStream(val appendToken: AppendToken) : EventStoreResult
}
