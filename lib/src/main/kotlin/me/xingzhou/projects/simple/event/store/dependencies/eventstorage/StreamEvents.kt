package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

data class StreamEvents(val events: List<StreamEvent>, val appendToken: String)
