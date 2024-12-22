package me.xingzhou.projects.simple.event.store

import java.time.Instant
import me.xingzhou.projects.simple.event.store.commands.CheckStreamExists
import me.xingzhou.projects.simple.event.store.commands.CreateStream
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromStream
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.results.EventStoreResult
import me.xingzhou.projects.simple.event.store.results.RetrievedEvent

class EventStore {
  @JvmName("handleCreateStream")
  fun handle(context: ExecutionContext<CreateStream>): EventStoreResult {
    val command = context.command
    val appendToken =
        context.forEventStorage.createStream(
            command.streamName.name,
            context.forEventSerialization.serialize(command.event),
            command.occurredOn.instant)
    return EventStoreResult.ForCreateStream(AppendToken(appendToken))
  }

  @JvmName("handleRetrieveFromStream")
  fun handle(context: ExecutionContext<RetrieveFromStream>): EventStoreResult {
    val command = context.command
    val events = context.forEventStorage.retrieveFromStream(command.streamName.name)
    val result =
        events.map {
          RetrievedEvent(
              event = context.forEventSerialization.deserialize(it.event),
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
}

data class AppendToken(val value: String)

data class OccurredOn(val instant: Instant)

data class StreamName(val name: String)

interface Event {
  val id: String
}
