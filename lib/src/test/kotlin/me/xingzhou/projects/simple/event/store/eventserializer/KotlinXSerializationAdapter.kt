package me.xingzhou.projects.simple.event.store.eventserializer

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer
import me.xingzhou.projects.simple.event.store.features.fixtures.AnEvent

@OptIn(ExperimentalSerializationApi::class)
class KotlinXSerializationAdapter : ForEventSerializer {
  val json = Json {
    serializersModule = SerializersModule { polymorphic(Event::class) { subclass(AnEvent::class) } }
  }

  override fun serialize(event: Event): ByteArray {
    val stream = ByteArrayOutputStream()
    json.encodeToStream(event, stream)
    return stream.toByteArray()
  }

  override fun deserialize(bytes: ByteArray): Event {
    val stream = ByteArrayInputStream(bytes)
    return json.decodeFromStream(stream)
  }
}
