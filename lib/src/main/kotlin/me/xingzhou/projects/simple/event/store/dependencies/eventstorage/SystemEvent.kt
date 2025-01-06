package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

data class SystemEvent(val streamName: String, val streamEvent: StreamEvent)
