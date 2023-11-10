package me.xingzhou.projects.simple.event.store

data class RetrievedEvent(val event: DomainEvent, val occurredOn: OccurredOn)
