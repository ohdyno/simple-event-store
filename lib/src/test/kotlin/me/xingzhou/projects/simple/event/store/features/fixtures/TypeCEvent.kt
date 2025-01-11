package me.xingzhou.projects.simple.event.store.features.fixtures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.xingzhou.projects.simple.event.store.Event

@Serializable
@SerialName("type-c-event")
data class TypeCEvent(override val id: String) : Event {
  constructor() : this("Type-C-Event-id")
}
