package me.xingzhou.projects.simple.event.store.features.fixtures

import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.ReplayObserver

class AllEventsObserver : ReplayObserver {
  val observedEvents = mutableListOf<Event>()

  fun observe(e: Event) {
    println(e)
    observedEvents.add(e)
  }
}
