package me.xingzhou.projects.simple.event.store.dependencies.eventserializer

import me.xingzhou.projects.simple.event.store.Event

interface ForEventSerializer {
  fun serialize(event: Event): SerializedEvent

  fun deserialize(type: String, data: String): Event

  data class SerializedEvent(val eventType: String, val eventData: String)
}
