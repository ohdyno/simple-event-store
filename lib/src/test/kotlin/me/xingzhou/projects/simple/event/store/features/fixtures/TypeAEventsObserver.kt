package me.xingzhou.projects.simple.event.store.features.fixtures

import java.time.Instant
import me.xingzhou.projects.simple.event.store.AppendToken

class TypeAEventsObserver : EventsRecorder, StreamAndSystemEventsRecorder {
  override lateinit var asOf: Instant
  override lateinit var appendToken: AppendToken
  override val observedEvents = mutableListOf<TypeAEvent>()

  fun observe(e: TypeAEvent) {
    observedEvents.add(e)
  }
}
