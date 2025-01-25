package me.xingzhou.projects.simple.event.store.features.fixtures

import me.xingzhou.projects.simple.event.store.Event

class TypeABEventsObserver : EventsRecorder {
  override val observedEvents = mutableListOf<Event>()

  fun observe(e: TypeAEvent) {
    observedEvents.add(e)
  }

  fun observe(e: TypeBEvent) {
    observedEvents.add(e)
  }
}
