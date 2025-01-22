package me.xingzhou.projects.simple.event.store.commands

import me.xingzhou.projects.simple.event.store.ReplayObserver

data class ReplayEventsFromSystem(val observerFn: () -> ReplayObserver)
