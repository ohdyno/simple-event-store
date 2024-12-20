package me.xingzhou.projects.simple.event.store.features

import kotlinx.serialization.Serializable
import me.xingzhou.projects.simple.event.store.Event

@Serializable
data class TypeA(override val id: String = "event-type-A") : Event
