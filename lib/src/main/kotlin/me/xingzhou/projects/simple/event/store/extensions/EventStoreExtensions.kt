package me.xingzhou.projects.simple.event.store.extensions

import kotlin.reflect.KFunction
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.functions
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.typeOf
import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.ReplayObserver
import me.xingzhou.projects.simple.event.store.commands.ReplayEventsFromStream
import me.xingzhou.projects.simple.event.store.commands.ReplayEventsFromSystem
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromStream
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromSystem
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.results.EventStoreResult

@JvmName("handleReplayEventsFromStream")
fun EventStore.handle(context: ExecutionContext<ReplayEventsFromStream>): EventStoreResult {
  return with(context.command.observerFn()) {
    context
        .copyOf(
            command =
                RetrieveFromStream(
                    streamName = context.command.streamName,
                    eventTypes = extractEventTypes(observer = this)))
        .let { handle(it) }
        .let {
          when {
            it is EventStoreResult.ForRetrieveFromStream -> {
              it.retrievedEvents.forEach { this.dynamicDispatch(it.event) }
              this.appendToken = it.appendToken
              EventStoreResult.ForReplayEvents(observer = this)
            }
            else -> it
          }
        }
  }
}

@JvmName("handleReplayEventsFromSystem")
fun EventStore.handle(context: ExecutionContext<ReplayEventsFromSystem>): EventStoreResult {
  return with(context.command.observerFn()) {
    context
        .copyOf(command = RetrieveFromSystem(eventTypes = extractEventTypes(observer = this)))
        .let { handle(it) }
        .let {
          when {
            it is EventStoreResult.ForRetrieveFromSystem -> {
              it.events.forEach { this.dynamicDispatch(it.event.event) }
              if (it.events.isNotEmpty()) {
                this.asOf = it.asOf
              }
              EventStoreResult.ForReplayEvents(observer = this)
            }
            else -> it
          }
        }
  }
}

private fun ReplayObserver.dynamicDispatch(event: Event) {
  observeFunctions()
      .first { event::class.createType().isSubtypeOf(it.parameters[1].type) }
      .call(this, event)
}

private fun EventStore.extractEventTypes(observer: ReplayObserver): List<KType> {
  return observer
      .observeFunctions()
      .map { it.parameters[1] }
      .map { it.type }
      .filterNot { it == typeOf<Event>() }
}

private fun ReplayObserver.observeFunctions(): List<KFunction<*>> {
  return this::class.functions.filter { it.name == "observe" }
}
