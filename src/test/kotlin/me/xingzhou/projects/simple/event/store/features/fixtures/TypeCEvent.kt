package me.xingzhou.projects.simple.event.store.features.fixtures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("type-c-event")
data class TypeCEvent(override val id: String) : EventWithId {
  constructor() : this("Type-C-Event-id")
}
