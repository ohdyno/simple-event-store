package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.time.Instant

data class StreamEvent(val eventName: String, val event: ByteArray, val occurredOn: Instant) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as StreamEvent

    if (eventName != other.eventName) return false
    if (!event.contentEquals(other.event)) return false
    if (occurredOn != other.occurredOn) return false

    return true
  }

  override fun hashCode(): Int {
    var result = eventName.hashCode()
    result = 31 * result + event.contentHashCode()
    result = 31 * result + occurredOn.hashCode()
    return result
  }
}
