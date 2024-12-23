package me.xingzhou.projects.simple.event.store.features.fixtures

import kotlinx.serialization.Serializable
import me.xingzhou.projects.simple.event.store.Event

@Serializable data class AnEvent(override val id: String = "event-type-A") : Event
