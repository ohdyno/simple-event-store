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

  data class ForCreateStream(override val appendToken: AppendToken) : WithAppendToken

  data class ForRetrieveFromStream(
      val retrievedEvents: List<RetrievedEvent>,
      override val appendToken: AppendToken
  ) : WithAppendToken

  data class ForCheckStreamExists(val streamExists: Boolean) : EventStoreResult

  data class ForValidateAppendToken(val appendTokenIsValid: Boolean) : EventStoreResult

  data class ForRetrieveAppendToken(override val appendToken: AppendToken) : WithAppendToken

  data class ForAppendToStream(override val appendToken: AppendToken) : WithAppendToken

  interface WithAppendToken : EventStoreResult {
    val appendToken: AppendToken
  }
}
