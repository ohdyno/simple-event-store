package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.time.Instant

data class SystemEvents(val events: List<SystemEvent>, val timestamp: Instant)
