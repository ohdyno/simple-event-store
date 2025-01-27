package me.xingzhou.projects.simple.event.store.results

import java.time.Instant
import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.ReplayObserver
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

  data class ForRetrieveFromSystem(val events: List<RetrievedSystemEvent>, val asOf: Instant) :
      EventStoreResult

  data class ForCheckStreamExists(val streamExists: Boolean) : EventStoreResult

  data class ForValidateAppendToken(val appendTokenIsValid: Boolean) : EventStoreResult

  data class ForRetrieveAppendToken(override val appendToken: AppendToken) : WithAppendToken

  data class ForAppendToStream(override val appendToken: AppendToken) : WithAppendToken

  data class ForReplayEvents(val observer: ReplayObserver) : EventStoreResult

  interface WithAppendToken : EventStoreResult {
    val appendToken: AppendToken
  }

  companion object
}

fun EventStoreResult.Companion.forCreateStream(
    appendToken: String
): EventStoreResult.ForCreateStream {
  return EventStoreResult.ForCreateStream(AppendToken(value = appendToken))
}

fun EventStoreResult.Companion.forRetrieveFromStream(
    events: List<RetrievedEvent>,
    appendToken: String
): EventStoreResult.ForRetrieveFromStream {
  return EventStoreResult.ForRetrieveFromStream(
      retrievedEvents = events, appendToken = AppendToken(value = appendToken))
}

fun EventStoreResult.Companion.forRetrieveFromSystem(
    events: List<RetrievedSystemEvent>,
    asOf: Instant
): EventStoreResult.ForRetrieveFromSystem {
  return EventStoreResult.ForRetrieveFromSystem(events = events, asOf = asOf)
}

fun EventStoreResult.Companion.forCheckStreamExists(
    streamExists: Boolean
): EventStoreResult.ForCheckStreamExists {
  return EventStoreResult.ForCheckStreamExists(streamExists = streamExists)
}

fun EventStoreResult.Companion.forValidateAppendToken(
    appendTokenIsValid: Boolean
): EventStoreResult.ForValidateAppendToken {
  return EventStoreResult.ForValidateAppendToken(appendTokenIsValid = appendTokenIsValid)
}

fun EventStoreResult.Companion.forRetrieveAppendToken(
    appendToken: String
): EventStoreResult.ForRetrieveAppendToken {
  return EventStoreResult.ForRetrieveAppendToken(appendToken = AppendToken(value = appendToken))
}

fun EventStoreResult.Companion.forAppendToStream(
    appendToken: String
): EventStoreResult.ForAppendToStream {
  return EventStoreResult.ForAppendToStream(appendToken = AppendToken(value = appendToken))
}
