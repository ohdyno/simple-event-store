package me.xingzhou.projects.simple.event.store

import java.time.Instant

interface ForEventSource {
    fun createStream(streamName: String, eventData: ByteArray, occurredOn: Instant): String
    fun retrieveFromStream(streamName: String): List<RetrievedEvent>
}
