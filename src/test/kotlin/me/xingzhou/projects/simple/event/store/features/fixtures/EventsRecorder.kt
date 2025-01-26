package me.xingzhou.projects.simple.event.store.features.fixtures

import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.ReplayObserver

interface EventsRecorder : ReplayObserver {
  val observedEvents: List<Event>
}
