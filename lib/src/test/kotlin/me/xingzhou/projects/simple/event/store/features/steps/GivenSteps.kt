package me.xingzhou.projects.simple.event.store.features.steps

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.OccurredOn
import me.xingzhou.projects.simple.event.store.StreamName
import me.xingzhou.projects.simple.event.store.commands.CheckStreamExists
import me.xingzhou.projects.simple.event.store.commands.CreateStream
import me.xingzhou.projects.simple.event.store.commands.RetrieveAppendToken
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage
import me.xingzhou.projects.simple.event.store.features.SpecificationContext
import me.xingzhou.projects.simple.event.store.features.fixtures.AnEvent
import me.xingzhou.projects.simple.event.store.results.EventStoreResult

class GivenSteps(private val context: SpecificationContext) {
  @Given("the event source system is setup for testing")
  fun theEventSourceSystemIsSetupForTesting() {
    context.eventStorage = ForEventStorage {}
    context.eventSerializer = ForEventSerializer {
      serializersModule = SerializersModule {
        polymorphic(Event::class) { subclass(AnEvent::class) }
      }
    }
  }

  @Given("an event")
  @Given("a valid event of type A")
  fun anEvent() {
    context.event = AnEvent()
  }

  @And("when the event occurred")
  fun theEventOccurredOnPMUTC() {
    context.occurredOn = OccurredOn("11/22/2023 12:34:56 PM UTC".asInstant())
  }

  @And("a new stream name")
  @And("a stream name")
  fun aNewStreamName() {
    context.streamName = StreamName("stream one")
    val executionContext =
        ExecutionContext(
            command = CheckStreamExists(streamName = context.streamName),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    val result = EventStore().handle(executionContext)

    result as EventStoreResult.ForCheckStreamExists

    result.result shouldBe false
  }

  @And("the event already exists in another stream")
  fun theEventAlreadyExistsInAnotherStream() {
    val executionContext =
        ExecutionContext(
            command =
                CreateStream(
                    streamName = StreamName("stream two"),
                    event = context.event,
                    occurredOn = context.occurredOn),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    EventStore().handle(executionContext)
  }

  @And("the stream already exists in the system")
  fun theStreamAlreadyExistsInTheSystem() {
    val executionContext =
        ExecutionContext(
            command =
                CreateStream(
                    streamName = context.streamName,
                    event = AnEvent(),
                    occurredOn = OccurredOn(Instant.EPOCH)),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    EventStore().handle(executionContext)
    context.eventStorageSnapshot = context.snapshotEventStorage()
  }

  @And("a valid append token for the stream")
  fun aValidAppendTokenForTheStream() {
    val executionContext =
        ExecutionContext(
            command = RetrieveAppendToken(streamName = context.streamName),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    val result = EventStore().handle(executionContext)
    result as EventStoreResult.ForRetrieveAppendToken

    context.appendToken = result.appendToken
  }

  @Given("a stream name for a stream that does not exist")
  fun aStreamNameForAStreamThatDoesNotExist() {
    context.streamName = StreamName("stream that does not exist")
  }
}

private fun String.asInstant(): Instant {
  val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a z")
  val zonedDateTime = ZonedDateTime.parse(this, formatter)
  return zonedDateTime.toInstant()
}
