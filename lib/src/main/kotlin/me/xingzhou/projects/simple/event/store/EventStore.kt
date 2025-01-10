package me.xingzhou.projects.simple.event.store

import java.time.Instant
import me.xingzhou.projects.simple.event.store.commands.*
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage
import me.xingzhou.projects.simple.event.store.results.EventStoreResult
import me.xingzhou.projects.simple.event.store.results.EventStoreResult.Failure
import me.xingzhou.projects.simple.event.store.results.RetrievedEvent

class EventStore {
  @JvmName("handleCreateStream")
  fun handle(context: ExecutionContext<CreateStream>): EventStoreResult =
      runCatching {
            context.forEventSerialization.serialize(event = context.command.event).run {
              context.forEventStorage
                  .createStream(
                      streamName = context.command.streamName.name,
                      eventId = context.command.event.id,
                      eventType = eventType,
                      eventData = eventData,
                      occurredOn = context.command.occurredOn.instant)
                  .let { AppendToken(value = it) }
                  .let { EventStoreResult.ForCreateStream(appendToken = it) }
            }
          }
          .getOrElse { it.extractKnownFailure(command = context.command) }

  @JvmName("handleRetrieveFromStream")
  fun handle(context: ExecutionContext<RetrieveFromStream>): EventStoreResult =
      runCatching {
            context.forEventStorage
                .retrieveFromStream(streamName = context.command.streamName.name)
                .map {
                  RetrievedEvent(
                      event =
                          context.forEventSerialization.deserialize(
                              type = it.eventType, data = it.eventData),
                      occurredOn = OccurredOn(instant = it.occurredOn))
                }
                .let { EventStoreResult.ForRetrieveFromStream(retrievedEvents = it) }
          }
          .getOrElse { it.extractKnownFailure(command = context.command) }

  @JvmName("handleCheckStreamExists")
  fun handle(context: ExecutionContext<CheckStreamExists>): EventStoreResult =
      context.forEventStorage.streamExists(streamName = context.command.streamName.name).let {
        EventStoreResult.ForCheckStreamExists(streamExists = it)
      }

  @JvmName("handleValidateAppendToken")
  fun handle(context: ExecutionContext<ValidateAppendToken>): EventStoreResult =
      runCatching {
            context.forEventStorage
                .validateAppendToken(
                    streamName = context.command.streamName.name,
                    token = context.command.token.value)
                .let { EventStoreResult.ForValidateAppendToken(appendTokenIsValid = it) }
          }
          .getOrElse { it.extractKnownFailure(command = context.command) }

  @JvmName("handleRetrieveAppendToken")
  fun handle(context: ExecutionContext<RetrieveAppendToken>): EventStoreResult =
      runCatching {
            context.forEventStorage
                .retrieveAppendToken(streamName = context.command.streamName.name)
                .let { AppendToken(value = it) }
                .let { EventStoreResult.ForRetrieveAppendToken(appendToken = it) }
          }
          .getOrElse { it.extractKnownFailure(command = context.command) }

  @JvmName("handleAppendToStream")
  fun handle(context: ExecutionContext<AppendToStream>): EventStoreResult =
      runCatching {
            context.forEventSerialization.serialize(event = context.command.event).run {
              context.forEventStorage
                  .appendToStream(
                      streamName = context.command.streamName.name,
                      appendToken = context.command.appendToken.value,
                      eventId = context.command.event.id,
                      eventType = eventType,
                      eventData = eventData,
                      occurredOn = context.command.occurredOn.instant)
                  .let { AppendToken(value = it) }
                  .let { EventStoreResult.ForAppendToStream(appendToken = it) }
            }
          }
          .getOrElse { it.extractKnownFailure(command = context.command) }
}

data class AppendToken(val value: String)

data class OccurredOn(val instant: Instant) {
  companion object {
    fun now() = OccurredOn(instant = Instant.now())
  }
}

data class StreamName(val name: String)

interface Event {
  val id: String
}

// Extension Functions
private fun Throwable.extractKnownFailure(command: CreateStream): EventStoreResult =
    when (this) {
      is ForEventStorage.Failure.StreamAlreadyExists ->
          Failure.StreamAlreadyExists(streamName = command.streamName, message = message!!)

      else -> throw this
    }

private fun Throwable.extractKnownFailure(command: RetrieveFromStream): EventStoreResult =
    when (this) {
      is ForEventStorage.Failure.StreamDoesNotExist ->
          Failure.StreamDoesNotExist(streamName = command.streamName, message = message!!)

      else -> throw this
    }

private fun Throwable.extractKnownFailure(command: ValidateAppendToken): EventStoreResult =
    when (this) {
      is ForEventStorage.Failure.StreamDoesNotExist ->
          Failure.StreamDoesNotExist(streamName = command.streamName, message = message!!)

      else -> throw this
    }

private fun Throwable.extractKnownFailure(command: RetrieveAppendToken): EventStoreResult =
    when (this) {
      is ForEventStorage.Failure.StreamDoesNotExist ->
          Failure.StreamDoesNotExist(streamName = command.streamName, message = message!!)

      else -> throw this
    }

private fun Throwable.extractKnownFailure(command: AppendToStream): EventStoreResult =
    when (this) {
      is ForEventStorage.Failure.StreamDoesNotExist ->
          Failure.StreamDoesNotExist(streamName = command.streamName, message = message!!)

      is ForEventStorage.Failure.InvalidAppendToken ->
          Failure.InvalidAppendToken(
              streamName = command.streamName,
              appendToken = command.appendToken,
              message = message!!)

      else -> throw this
    }
