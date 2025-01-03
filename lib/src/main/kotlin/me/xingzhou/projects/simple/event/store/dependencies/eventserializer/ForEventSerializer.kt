package me.xingzhou.projects.simple.event.store.dependencies.eventserializer

import me.xingzhou.projects.simple.event.store.Event

interface ForEventSerializer {
  fun serialize(event: Event): SerializedEvent

  fun deserialize(name: String, bytes: ByteArray): Event

  data class SerializedEvent(val eventName: String, val eventData: ByteArray) {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (javaClass != other?.javaClass) return false

      other as SerializedEvent

      if (eventName != other.eventName) return false
      if (!eventData.contentEquals(other.eventData)) return false

      return true
    }

    override fun hashCode(): Int {
      var result = eventName.hashCode()
      result = 31 * result + eventData.contentHashCode()
      return result
    }
  }
}
