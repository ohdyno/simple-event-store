package me.xingzhou.projects.simple.event.store.features.replayobservers

import java.time.Instant
import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.Event

class AllEventsObserver : StreamAndSystemEventsRecorder {
  override val observedEvents = mutableListOf<Event>()
  override lateinit var appendToken: AppendToken
  override lateinit var asOf: Instant

  fun observe(e: Event) {
    observedEvents.add(e)
  }
}
