package me.xingzhou.projects.simple.event.store

abstract class DomainEvent(val id: EventId) {}

data class EventId(val eventId: String) {}
