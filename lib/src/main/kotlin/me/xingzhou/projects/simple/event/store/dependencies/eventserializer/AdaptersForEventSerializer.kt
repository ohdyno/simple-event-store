package me.xingzhou.projects.simple.event.store.dependencies.eventserializer

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer.SerializedEvent

fun ForEventSerializer(
    configure: KotlinXSerializationAdapterBuilder.() -> Unit
): ForEventSerializer {
  val builder = KotlinXSerializationAdapterBuilder()
  builder.configure()
  return builder.build()
}

class KotlinXSerializationAdapterBuilder {
  lateinit var serializersModule: SerializersModule

  fun build(): ForEventSerializer {
    return KotlinXSerializationAdapter(serializersModule)
  }

  @OptIn(ExperimentalSerializationApi::class)
  private class KotlinXSerializationAdapter(serializerModule: SerializersModule) :
      ForEventSerializer {
    private val json: Json by lazy { Json { this.serializersModule = serializerModule } }

    override fun serialize(event: Event): SerializedEvent {
      val eventType = json.encodeToJsonElement(event).jsonObject["type"]!!.jsonPrimitive.content
      val stream = ByteArrayOutputStream()
      json.encodeToStream(event, stream)
      val eventData = stream.toByteArray()
      return SerializedEvent(eventType = eventType, eventData = eventData)
    }

    override fun deserialize(type: String, bytes: ByteArray): Event {
      val stream = ByteArrayInputStream(bytes)
      return json.decodeFromStream(stream)
    }
  }
}
