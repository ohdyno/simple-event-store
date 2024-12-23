package me.xingzhou.projects.simple.event.store.features.steps

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.kotest.matchers.shouldBe
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import me.xingzhou.projects.simple.event.store.*
import me.xingzhou.projects.simple.event.store.commands.CheckStreamExists
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer
import me.xingzhou.projects.simple.event.store.features.SpecificationContext
import me.xingzhou.projects.simple.event.store.features.fixtures.AnEvent
import me.xingzhou.projects.simple.event.store.results.EventStoreResult

class GivenSteps(private val context: SpecificationContext) {
  @OptIn(ExperimentalSerializationApi::class)
  @Given("the event source system is setup for testing")
  fun theEventSourceSystemIsSetupForTesting() {
    val adapter = InMemoryAdapterForEventStorage()
    context.adapter = adapter
    val serializer =
        object : ForEventSerializer {
          val json = Json {
            serializersModule = SerializersModule {
              polymorphic(Event::class) { subclass(AnEvent::class) }
            }
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
    context.serializer = serializer
  }

  @Given("an event")
  @Given("a valid event of type A")
  fun anEvent() {
    context.event = AnEvent()
  }

  @And("^the event occurred on 11/22/2023 12:34:56 PM UTC$")
  @And("when the event occurred")
  fun theEventOccurredOnPMUTC() {
    context.occurredOn = OccurredOn("11/22/2023 12:34:56 PM UTC".asInstant())
  }

  @And("^a desired stream name of \"stream one\"$")
  @And("a new stream name")
  fun aDesiredStreamNameOfStreamOne() {
    context.streamName = StreamName("stream one")
    val executionContext =
        ExecutionContext(
            command = CheckStreamExists(streamName = context.streamName),
            forEventStorage = context.adapter,
            forEventSerialization = context.serializer)
    val result = EventStore().handle(executionContext)

    result as EventStoreResult.ForCheckStreamExists

    result.result shouldBe false
  }
}

fun String.asInstant(): Instant {
  val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a z")
  val zonedDateTime = ZonedDateTime.parse(this, formatter)
  return zonedDateTime.toInstant()
}
