package me.xingzhou.projects.simple.event.store.features.replayobservers

import java.time.Instant
import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.features.events.TypeAEvent

class TypeAEventsObserver : EventsRecorder, StreamAndSystemEventsRecorder {
  override lateinit var asOf: Instant
  override lateinit var appendToken: AppendToken
  override val observedEvents = mutableListOf<TypeAEvent>()

  fun observe(e: TypeAEvent) {
    observedEvents.add(e)
  }
}
