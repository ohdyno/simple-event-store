package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.time.Instant

data class StreamEvent(val eventType: String, val eventData: String, val occurredOn: Instant)
