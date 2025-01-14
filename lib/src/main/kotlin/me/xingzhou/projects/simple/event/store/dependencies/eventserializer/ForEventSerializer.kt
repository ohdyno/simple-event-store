package me.xingzhou.projects.simple.event.store.dependencies.eventserializer

import kotlin.reflect.KClass
import kotlin.reflect.KType
import me.xingzhou.projects.simple.event.store.Event

interface ForEventSerializer {
  fun serialize(event: Event): SerializedEvent

  fun deserialize(type: String, data: String): Event

  fun eventTypeOf(klass: KClass<out Event>): String

  fun eventTypeOf(type: KType): String

  data class SerializedEvent(val eventType: String, val eventData: String)
}
