package me.xingzhou.projects.simple.event.store.commands

import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.OccurredOn
import me.xingzhou.projects.simple.event.store.StreamName

data class CreateStream(val streamName: StreamName, val event: Event, val occurredOn: OccurredOn)
