package me.xingzhou.projects.simple.event.store.features

import me.xingzhou.projects.simple.event.store.DomainEvent

data class TypeA(override val id: String = "event-type-A") : DomainEvent
