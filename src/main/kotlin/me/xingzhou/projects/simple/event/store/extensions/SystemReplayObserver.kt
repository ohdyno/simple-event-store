package me.xingzhou.projects.simple.event.store.extensions

import java.time.Instant
import me.xingzhou.projects.simple.event.store.ReplayObserver

interface SystemReplayObserver : ReplayObserver {
  var asOf: Instant
}
