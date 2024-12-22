package me.xingzhou.projects.simple.event.store.results

import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.OccurredOn

data class RetrievedEvent(val event: Event, val occurredOn: OccurredOn)
