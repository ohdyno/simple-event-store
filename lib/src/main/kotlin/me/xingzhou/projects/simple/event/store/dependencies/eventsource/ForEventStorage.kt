package me.xingzhou.projects.simple.event.store.dependencies.eventsource

import java.time.Instant
import me.xingzhou.projects.simple.event.store.results.RetrievedEvent

interface ForEventStorage {
  fun createStream(streamName: String, eventData: ByteArray, occurredOn: Instant): String

  fun retrieveFromStream(streamName: String): List<RetrievedEvent>
}
