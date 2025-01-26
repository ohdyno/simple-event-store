package me.xingzhou.projects.simple.event.store.commands

import me.xingzhou.projects.simple.event.store.extensions.SystemReplayObserver

data class ReplayEventsFromSystem(val observerFn: () -> SystemReplayObserver)
