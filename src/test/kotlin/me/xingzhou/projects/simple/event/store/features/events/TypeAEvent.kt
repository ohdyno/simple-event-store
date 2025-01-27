package me.xingzhou.projects.simple.event.store.features.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("type-a-event")
data class TypeAEvent(override val id: String = "Type-A-Event-id") : EventWithId
