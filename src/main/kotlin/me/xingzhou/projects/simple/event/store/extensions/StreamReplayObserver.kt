package me.xingzhou.projects.simple.event.store.extensions

import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.ReplayObserver

interface StreamReplayObserver : ReplayObserver {
  var appendToken: AppendToken
}
