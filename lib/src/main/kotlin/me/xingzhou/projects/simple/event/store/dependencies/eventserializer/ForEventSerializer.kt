package me.xingzhou.projects.simple.event.store.dependencies.eventserializer

import me.xingzhou.projects.simple.event.store.Event

interface ForEventSerializer {
  fun serialize(event: Event): ByteArray

  fun deserialize(bytes: ByteArray): Event
}
