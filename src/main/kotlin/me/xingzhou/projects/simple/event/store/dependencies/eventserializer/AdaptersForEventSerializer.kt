package me.xingzhou.projects.simple.event.store.dependencies.eventserializer

import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotations
import kotlin.reflect.jvm.jvmErasure
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
  lateinit var json: Json

  fun build(): ForEventSerializer {
    return KotlinXSerializationAdapter(json)
  }

  private class KotlinXSerializationAdapter(private val json: Json) : ForEventSerializer {

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
      return when (val serialName = klass.findAnnotations<SerialName>()) {
        emptyList<SerialName>() -> klass.toString()
        else -> serialName.first().value
      }
    }

    override fun eventTypeOf(type: KType): String {
      return when (val serialName = type.jvmErasure.findAnnotations<SerialName>()) {
        emptyList<SerialName>() -> type.toString()
        else -> serialName.first().value
      }
    }
  }
}
