package me.xingzhou.projects.simple.event.store.features

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import java.io.ByteArrayOutputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import me.xingzhou.projects.simple.event.store.*

class GivenSteps(private val context: SpecificationContext) {
  @OptIn(ExperimentalSerializationApi::class)
  @Given("the event source system is setup for testing")
  fun theEventSourceSystemIsSetupForTesting() {
    val adapter = InMemoryAdapterForEventSource()
    context.adapter = adapter
    val serializer =
        object : EventSerializer {
          val json = Json {
            serializersModule = SerializersModule {
              polymorphic(Event::class) { subclass(TypeA::class) }
            }
          }

          override fun serialize(event: Event): ByteArray {
            val stream = ByteArrayOutputStream()
            json.encodeToStream(event, stream)
            return stream.toByteArray()
          }
        }
    context.serializer = serializer
    context.store = EventStore(adapter, serializer)
  }

  @Given("a valid event of type A")
  fun aValidEventOfTypeA() {
    context.event = TypeA()
  }

  @And("^the event occurred on 11/22/2023 12:34:56 PM UTC$")
  fun theEventOccurredOnPMUTC() {
    context.occurredOn = OccurredOn("11/22/2023 12:34:56 PM UTC".asInstant())
  }

  @And("^a desired stream name of \"stream one\"$")
  fun aDesiredStreamNameOfStreamOne() {
    context.streamName = StreamName("stream one")
  }
}
