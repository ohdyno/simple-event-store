package me.xingzhou.projects.simple.event.store.features.replayobservers

import java.time.Instant
import me.xingzhou.projects.simple.event.store.extensions.SystemReplayObserver

interface SystemEventsRecorder : SystemReplayObserver, EventsRecorder {
  override var asOf: Instant
}
