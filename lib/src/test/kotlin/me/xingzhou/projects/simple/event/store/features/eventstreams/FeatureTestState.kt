package me.xingzhou.projects.simple.event.store.features.eventstreams

import me.xingzhou.projects.simple.event.store.DomainEvent
import me.xingzhou.projects.simple.event.store.ForEventSource

class FeatureTestState {
  lateinit var retrievedEvents: List<DomainEvent>
  lateinit var subject: ForEventSource
  lateinit var event: DomainEvent
}
