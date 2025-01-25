package me.xingzhou.projects.simple.event.store.features.fixtures

import java.time.Instant
import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.Event

class TypeABEventsObserver : EventsRecorder, StreamAndSystemEventsRecorder {
  override lateinit var asOf: Instant
  override lateinit var appendToken: AppendToken
  override val observedEvents = mutableListOf<Event>()

  fun observe(e: TypeAEvent) {
    observedEvents.add(e)
  }

  fun observe(e: TypeBEvent) {
    observedEvents.add(e)
  }
}
