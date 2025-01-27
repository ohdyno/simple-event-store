package me.xingzhou.projects.simple.event.store.features.fixtures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("type-a-event")
data class TypeAEvent(override val id: String) : EventWithId {
  constructor() : this("Type-A-Event-id")
}
