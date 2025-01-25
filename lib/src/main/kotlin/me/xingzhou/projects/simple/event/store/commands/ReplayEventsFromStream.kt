package me.xingzhou.projects.simple.event.store.commands

import me.xingzhou.projects.simple.event.store.StreamName
import me.xingzhou.projects.simple.event.store.extensions.StreamReplayObserver

data class ReplayEventsFromStream(
    val observerFn: () -> StreamReplayObserver,
    val streamName: StreamName
)
