package me.xingzhou.projects.simple.event.store.features.fixtures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.xingzhou.projects.simple.event.store.Event

@Serializable
@SerialName("type-a-event")
data class TypeAEvent(override val id: String) : Event {
  constructor() : this("Type-A-Event-id")
}
