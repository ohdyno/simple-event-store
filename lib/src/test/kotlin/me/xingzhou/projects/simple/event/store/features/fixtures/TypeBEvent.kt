package me.xingzhou.projects.simple.event.store.features.fixtures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.xingzhou.projects.simple.event.store.Event

@Serializable
@SerialName("type-b-event")
data class TypeBEvent(override val id: String) : Event {
  constructor() : this("Type-B-Event-id")
}
