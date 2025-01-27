package me.xingzhou.projects.simple.event.store.commands

import me.xingzhou.projects.simple.event.store.*

data class AppendToStream(
    val streamName: StreamName,
    val event: Event,
    val eventId: EventId,
    val occurredOn: OccurredOn,
    val appendToken: AppendToken
)
