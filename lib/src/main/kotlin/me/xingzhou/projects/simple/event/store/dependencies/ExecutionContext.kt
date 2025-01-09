package me.xingzhou.projects.simple.event.store.dependencies

import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage

data class ExecutionContext<Command>(
    val command: Command,
    val forEventSerialization: ForEventSerializer,
    val forEventStorage: ForEventStorage
) {
  fun <T> copyOf(command: T): ExecutionContext<T> =
      ExecutionContext(command, forEventSerialization, forEventStorage)
}
