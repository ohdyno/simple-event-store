package me.xingzhou.projects.simple.event.store.dependencies.eventserializer

import me.xingzhou.projects.simple.event.store.Event

interface ForEventSerializer {
  fun serialize(event: Event): SerializedEvent

  fun deserialize(type: String, bytes: ByteArray): Event

  data class SerializedEvent(val eventType: String, val eventData: ByteArray) {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as SerializedEvent

      if (eventType != other.eventType) return false
      if (!eventData.contentEquals(other.eventData)) return false

      return true
    }

    override fun hashCode(): Int {
      var result = eventType.hashCode()
      result = 31 * result + eventData.contentHashCode()
      return result
    }
  }
}
