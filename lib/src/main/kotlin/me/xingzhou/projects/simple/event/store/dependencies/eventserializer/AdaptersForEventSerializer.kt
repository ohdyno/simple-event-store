package me.xingzhou.projects.simple.event.store.dependencies.eventserializer

import kotlin.reflect.KClass
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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

  private class KotlinXSerializationAdapter(serializerModule: SerializersModule) :
      ForEventSerializer {
    private val json: Json by lazy { Json { this.serializersModule = serializerModule } }

    override fun serialize(event: Event): SerializedEvent {
      val jsonElement = json.encodeToJsonElement<Event>(event)
      return SerializedEvent(
          eventType = jsonElement.jsonObject["type"]!!.jsonPrimitive.content,
          eventData = jsonElement.toString())
    }

    override fun deserialize(type: String, data: String): Event {
      return json.decodeFromString(data)
    }

    override fun eventTypeOf(klass: KClass<out Event>): String {
      val jsonElement =
          json.encodeToJsonElement<Event>(
              klass.constructors.first { it.parameters.isEmpty() }.call())
      return jsonElement.jsonObject["type"]!!.jsonPrimitive.content
    }
  }
}
