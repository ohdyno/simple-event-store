package me.xingzhou.projects.simple.event.store.commands

import kotlin.reflect.KType
import me.xingzhou.projects.simple.event.store.StreamName

data class RetrieveFromStream(
    val streamName: StreamName,
    val eventTypes: List<KType> = emptyList()
)
