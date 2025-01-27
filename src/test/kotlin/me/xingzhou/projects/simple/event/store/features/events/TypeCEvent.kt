package me.xingzhou.projects.simple.event.store.features.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("type-c-event")
data class TypeCEvent(override val id: String) : EventWithId {}
