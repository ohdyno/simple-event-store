package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.time.Instant

interface ForEventStorage {
  fun createStream(streamName: String, eventData: ByteArray, occurredOn: Instant): String

  fun retrieveFromStream(streamName: String): List<StreamEvent>

  fun streamExists(streamName: String): Boolean
}

data class StreamEvent(val event: ByteArray, val occurredOn: Instant) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as StreamEvent

    if (!event.contentEquals(other.event)) return false
    if (occurredOn != other.occurredOn) return false

    return true
  }

  override fun hashCode(): Int {
    var result = event.contentHashCode()
    result = 31 * result + occurredOn.hashCode()
    return result
  }
}
