package me.xingzhou.projects.simple.event.store.commands

import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.OccurredOn
import me.xingzhou.projects.simple.event.store.StreamName

data class AppendToStream(
    val streamName: StreamName,
    val event: Event,
    val occurredOn: OccurredOn,
    val appendToken: AppendToken
)
