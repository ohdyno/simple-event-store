package me.xingzhou.projects.simple.event.store

import java.time.Instant
import me.xingzhou.projects.simple.event.store.commands.CreateStream
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromStream
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.results.EventStoreResult

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
    var events = context.forEventStorage.retrieveFromStream(command.streamName.name)
    return EventStoreResult.ForRetrieveFromStream(events)
  }
}

data class AppendToken(val value: String)

data class OccurredOn(val instant: Instant)

data class StreamName(val name: String)

interface Event {
  val id: String
}
