package me.xingzhou.projects.simple.event.store.commands

import kotlin.reflect.KClass
import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.StreamName

data class RetrieveFromStream(
    val streamName: StreamName,
    val eventTypes: List<KClass<out Event>> = emptyList()
)
