package me.xingzhou.projects.simple.event.store.features.fixtures

import java.time.Instant
import me.xingzhou.projects.simple.event.store.extensions.SystemReplayObserver

interface SystemEventsRecorder : SystemReplayObserver, EventsRecorder {
  override var asOf: Instant
}
