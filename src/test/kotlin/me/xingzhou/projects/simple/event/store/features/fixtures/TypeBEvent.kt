package me.xingzhou.projects.simple.event.store.features.fixtures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("type-b-event")
data class TypeBEvent(override val id: String) : EventWithId {
  constructor() : this("Type-B-Event-id")
}
