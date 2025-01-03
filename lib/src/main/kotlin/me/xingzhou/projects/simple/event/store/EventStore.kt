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
  fun handle(context: ExecutionContext<CreateStream>): EventStoreResult {
    try {
      val command = context.command
      val (eventName, eventData) = context.forEventSerialization.serialize(command.event)
      val appendToken =
          context.forEventStorage.createStream(
              command.streamName.name, eventName, eventData, command.occurredOn.instant)
      return EventStoreResult.ForCreateStream(AppendToken(appendToken))
    } catch (failure: ForEventStorage.Failure) {
      return when (failure) {
        is ForEventStorage.Failure.StreamAlreadyExists ->
            Failure.StreamAlreadyExists(context.command.streamName, failure.message!!)

        else -> throw failure
      }
    }
  }

  @JvmName("handleRetrieveFromStream")
  fun handle(context: ExecutionContext<RetrieveFromStream>): EventStoreResult {
    val command = context.command
    val events = context.forEventStorage.retrieveFromStream(command.streamName.name)
    val result =
        events.map {
          RetrievedEvent(
              event = context.forEventSerialization.deserialize(it.eventName, it.event),
              occurredOn = OccurredOn(it.occurredOn))
        }
    return EventStoreResult.ForRetrieveFromStream(result)
  }

  @JvmName("handleCheckStreamExists")
  fun handle(context: ExecutionContext<CheckStreamExists>): EventStoreResult {
    val command = context.command
    val result = context.forEventStorage.streamExists(command.streamName.name)
    return EventStoreResult.ForCheckStreamExists(result)
  }

  @JvmName("handleValidateAppendToken")
  fun handle(context: ExecutionContext<ValidateAppendToken>): EventStoreResult {
    val command = context.command
    val result =
        context.forEventStorage.validateAppendToken(command.streamName.name, command.token.value)
    return EventStoreResult.ForValidateAppendToken(result)
  }

  @JvmName("handleRetrieveAppendToken")
  fun handle(context: ExecutionContext<RetrieveAppendToken>): EventStoreResult {
    try {
      val command = context.command
      val result = context.forEventStorage.retrieveAppendToken(command.streamName.name)
      return EventStoreResult.ForRetrieveAppendToken(AppendToken(result))
    } catch (failure: ForEventStorage.Failure) {
      return when (failure) {
        is ForEventStorage.Failure.StreamDoesNotExist ->
            Failure.StreamDoesNotExist(context.command.streamName, failure.message!!)
        else -> throw failure
      }
    }
  }

  @JvmName("handleAppendToStream")
  fun handle(context: ExecutionContext<AppendToStream>): EventStoreResult {
    try {
      val command = context.command
      val (eventName, eventData) = context.forEventSerialization.serialize(command.event)
      val result =
          context.forEventStorage.appendToStream(
              command.streamName.name,
              command.appendToken.value,
              eventName,
              eventData,
              command.occurredOn.instant)
      return EventStoreResult.ForAppendToStream(AppendToken(result))
    } catch (failure: ForEventStorage.Failure) {
      return when (failure) {
        is ForEventStorage.Failure.StreamDoesNotExist ->
            Failure.StreamDoesNotExist(context.command.streamName, failure.message!!)

        is ForEventStorage.Failure.InvalidAppendToken ->
            Failure.InvalidAppendToken(
                context.command.streamName, context.command.appendToken, failure.message!!)

        else -> throw failure
      }
    }
  }
}

data class AppendToken(val value: String)

data class OccurredOn(val instant: Instant)

data class StreamName(val name: String)

interface Event {
  val id: String
}
