package me.xingzhou.projects.simple.event.store.features.fixtures

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer

val TestEventsSerializer = ForEventSerializer {
  serializersModule = SerializersModule {
    polymorphic(Event::class) {
      subclass(TypeAEvent::class)
      subclass(TypeBEvent::class)
      subclass(TypeCEvent::class)
    }
  }
}
