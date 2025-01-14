package me.xingzhou.projects.simple.event.store.commands

import me.xingzhou.projects.simple.event.store.ReplayObserver
import me.xingzhou.projects.simple.event.store.StreamName

data class ReplayEventsFromStream(val observerFn: () -> ReplayObserver, val streamName: StreamName)
