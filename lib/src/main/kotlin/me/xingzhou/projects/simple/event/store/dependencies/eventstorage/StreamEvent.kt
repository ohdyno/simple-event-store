package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.time.Instant

data class StreamEvent(val eventType: String, val eventData: ByteArray, val occurredOn: Instant) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as StreamEvent

    if (eventType != other.eventType) return false
    if (!eventData.contentEquals(other.eventData)) return false
    if (occurredOn != other.occurredOn) return false

    return true
  }

  override fun hashCode(): Int {
    var result = eventType.hashCode()
    result = 31 * result + eventData.contentHashCode()
    result = 31 * result + occurredOn.hashCode()
    return result
  }
}
