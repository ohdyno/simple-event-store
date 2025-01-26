package me.xingzhou.projects.simple.event.store.results

import me.xingzhou.projects.simple.event.store.StreamName

data class RetrievedSystemEvent(val streamName: StreamName, val event: RetrievedEvent)
