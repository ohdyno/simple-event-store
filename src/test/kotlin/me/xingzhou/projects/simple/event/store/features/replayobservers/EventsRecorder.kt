package me.xingzhou.projects.simple.event.store.features.replayobservers

import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.ReplayObserver

interface EventsRecorder : ReplayObserver {
  val observedEvents: List<Event>
}
