package me.xingzhou.projects.simple.event.store.features.fixtures

import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.Event

class AllEventsObserver : StreamEventsRecorder {
  override val observedEvents = mutableListOf<Event>()
  override lateinit var appendToken: AppendToken

  fun observe(e: Event) {
    observedEvents.add(e)
  }
}
