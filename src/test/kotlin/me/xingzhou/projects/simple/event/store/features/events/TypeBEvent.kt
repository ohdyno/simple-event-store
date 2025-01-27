package me.xingzhou.projects.simple.event.store.features.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("type-b-event")
data class TypeBEvent(override val id: String) : EventWithId {}
