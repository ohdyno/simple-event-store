package me.xingzhou.projects.simple.event.store.dependencies

import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer
import me.xingzhou.projects.simple.event.store.dependencies.eventsource.ForEventStorage

data class ExecutionContext<Command>(
    val command: Command,
    val forEventSerialization: ForEventSerializer,
    val forEventStorage: ForEventStorage
)
