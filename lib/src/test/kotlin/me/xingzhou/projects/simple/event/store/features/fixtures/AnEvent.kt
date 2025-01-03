package me.xingzhou.projects.simple.event.store.features.fixtures

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.xingzhou.projects.simple.event.store.Event

@Serializable
@SerialName("a-test-event")
data class AnEvent(override val id: String = "event-type-A") : Event
