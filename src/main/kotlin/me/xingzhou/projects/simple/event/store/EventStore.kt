package me.xingzhou.projects.simple.event.store

import kotlin.reflect.KType
import me.xingzhou.projects.simple.event.store.commands.*
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.StreamEvent
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.SystemEvent
import me.xingzhou.projects.simple.event.store.results.*
import me.xingzhou.projects.simple.event.store.results.EventStoreResult.Failure

class EventStore {
  @JvmName("handleCreateStream")
  fun handle(context: ExecutionContext<CreateStream>): EventStoreResult =
      runCatching {
            context.forEventSerialization
                .serialize(event = context.command.event)
                .let { (eventType, eventData) ->
                  context.forEventStorage.createStream(
                      streamName = context.command.streamName.name,
                      eventId = context.command.eventId.value,
                      eventType = eventType,
                      eventData = eventData,
                      occurredOn = context.command.occurredOn.instant)
                }
                .let { EventStoreResult.forCreateStream(appendToken = it) }
          }
          .getOrElse { it.extractKnownFailure(command = context.command) }

  @JvmName("handleRetrieveFromStream")
  fun handle(context: ExecutionContext<RetrieveFromStream>): EventStoreResult =
      runCatching {
            context.command.eventTypes.serialize(context.forEventSerialization).let { eventTypes ->
              context.forEventStorage
                  .retrieveFromStream(
                      streamName = context.command.streamName.name, eventTypes = eventTypes)
                  .let { (events, appendToken) ->
                    EventStoreResult.forRetrieveFromStream(
                        events = events.deserialize(context.forEventSerialization),
                        appendToken = appendToken)
                  }
            }
          }
          .getOrElse { it.extractKnownFailure(command = context.command) }

  @JvmName("handleRetrieveFromSystem")
  fun handle(context: ExecutionContext<RetrieveFromSystem>): EventStoreResult =
      context.command.eventTypes.serialize(context.forEventSerialization).let { eventTypes ->
        context.forEventStorage.retrieveFromSystem(eventTypes = eventTypes).let {
            (events, timestamp) ->
          EventStoreResult.forRetrieveFromSystem(
              events = events.deserialize(context.forEventSerialization), asOf = timestamp)
        }
      }

  @JvmName("handleCheckStreamExists")
  fun handle(context: ExecutionContext<CheckStreamExists>): EventStoreResult =
      context.forEventStorage.streamExists(streamName = context.command.streamName.name).let {
        EventStoreResult.forCheckStreamExists(streamExists = it)
      }

  @JvmName("handleValidateAppendToken")
  fun handle(context: ExecutionContext<ValidateAppendToken>): EventStoreResult =
      runCatching {
            context.forEventStorage
                .validateAppendToken(
                    streamName = context.command.streamName.name,
                    token = context.command.token.value)
                .let { EventStoreResult.forValidateAppendToken(appendTokenIsValid = it) }
          }
          .getOrElse { it.extractKnownFailure(command = context.command) }

  @JvmName("handleRetrieveAppendToken")
  fun handle(context: ExecutionContext<RetrieveAppendToken>): EventStoreResult =
      runCatching {
            context.forEventStorage
                .retrieveAppendToken(streamName = context.command.streamName.name)
                .let { EventStoreResult.forRetrieveAppendToken(appendToken = it) }
          }
          .getOrElse { it.extractKnownFailure(command = context.command) }

  @JvmName("handleAppendToStream")
  fun handle(context: ExecutionContext<AppendToStream>): EventStoreResult =
      runCatching {
            context.forEventSerialization.serialize(event = context.command.event).let {
                (eventType, eventData) ->
              context.forEventStorage
                  .appendToStream(
                      streamName = context.command.streamName.name,
                      appendToken = context.command.appendToken.value,
                      eventId = context.command.eventId.value,
                      eventType = eventType,
                      eventData = eventData,
                      occurredOn = context.command.occurredOn.instant)
                  .let { EventStoreResult.forAppendToStream(appendToken = it) }
            }
          }
          .getOrElse { it.extractKnownFailure(command = context.command) }
}

@JvmName("deserializeStreamEvents")
private fun List<StreamEvent>.deserialize(serializer: ForEventSerializer): List<RetrievedEvent> {
  return map { (eventType, eventData, occurredOn) ->
    RetrievedEvent(
        event = serializer.deserialize(type = eventType, data = eventData),
        occurredOn = OccurredOn(instant = occurredOn))
  }
}

@JvmName("deserializeSystemEvents")
private fun List<SystemEvent>.deserialize(
    serializer: ForEventSerializer
): List<RetrievedSystemEvent> {
  return map { (streamName, streamEvent) ->
    RetrievedSystemEvent(
        streamName = StreamName(name = streamName),
        event =
            RetrievedEvent(
                event =
                    serializer.deserialize(
                        type = streamEvent.eventType, data = streamEvent.eventData),
                occurredOn = OccurredOn(instant = streamEvent.occurredOn)))
  }
}

// Extension Functions

private fun List<KType>.serialize(serializer: ForEventSerializer): List<String> = map {
  serializer.eventTypeOf(it)
}

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
