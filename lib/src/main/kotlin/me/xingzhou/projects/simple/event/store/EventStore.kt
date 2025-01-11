package me.xingzhou.projects.simple.event.store

import java.time.Instant
import kotlin.reflect.KClass
import me.xingzhou.projects.simple.event.store.commands.*
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.StreamEvent
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.SystemEvent
import me.xingzhou.projects.simple.event.store.results.EventStoreResult
import me.xingzhou.projects.simple.event.store.results.EventStoreResult.Failure
import me.xingzhou.projects.simple.event.store.results.RetrievedEvent
import me.xingzhou.projects.simple.event.store.results.RetrievedSystemEvent

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
            context.command.eventTypes.serialize(context.forEventSerialization).let {
              context.forEventStorage
                  .retrieveFromStream(streamName = context.command.streamName.name, eventTypes = it)
                  .run {
                    events.map { it.deserialize(context.forEventSerialization) } to
                        AppendToken(value = appendToken)
                  }
                  .let { (events, appendToken) ->
                    EventStoreResult.ForRetrieveFromStream(
                        retrievedEvents = events, appendToken = appendToken)
                  }
            }
          }
          .getOrElse { it.extractKnownFailure(command = context.command) }

  @JvmName("handleRetrieveFromSystem")
  fun handle(context: ExecutionContext<RetrieveFromSystem>): EventStoreResult =
      context.command.eventTypes.serialize(context.forEventSerialization).let {
        context.forEventStorage
            .retrieveFromSystem(eventTypes = it)
            .run { events.map { it.deserialize(context.forEventSerialization) } }
            .let { EventStoreResult.ForRetrieveFromSystem(events = it) }
      }

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

private fun StreamEvent.deserialize(
    serializer: ForEventSerializer,
): RetrievedEvent =
    with(serializer.deserialize(type = eventType, data = eventData)) {
      RetrievedEvent(event = this, occurredOn = OccurredOn(instant = occurredOn))
    }

private fun SystemEvent.deserialize(
    serializer: ForEventSerializer,
): RetrievedSystemEvent =
    with(serializer.deserialize(type = streamEvent.eventType, data = streamEvent.eventData)) {
      RetrievedSystemEvent(
          streamName = StreamName(name = streamName),
          event =
              RetrievedEvent(
                  event = this, occurredOn = OccurredOn(instant = streamEvent.occurredOn)))
    }

private fun List<KClass<out Event>>.serialize(serializer: ForEventSerializer): List<String> = map {
  serializer.eventTypeOf(it)
}

data class AppendToken(val value: String)

data class OccurredOn(val instant: Instant) : Comparable<OccurredOn> {
  override fun compareTo(other: OccurredOn): Int = instant.compareTo(other.instant)

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
