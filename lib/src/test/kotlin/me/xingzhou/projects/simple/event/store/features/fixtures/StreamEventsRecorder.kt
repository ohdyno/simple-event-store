package me.xingzhou.projects.simple.event.store.features.fixtures

import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.extensions.StreamReplayObserver

interface StreamEventsRecorder : EventsRecorder, StreamReplayObserver {
  override var appendToken: AppendToken
}
