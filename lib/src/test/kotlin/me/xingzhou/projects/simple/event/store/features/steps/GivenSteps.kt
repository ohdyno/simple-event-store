package me.xingzhou.projects.simple.event.store.features.steps

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.kotest.matchers.shouldBe
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.OccurredOn
import me.xingzhou.projects.simple.event.store.StreamName
import me.xingzhou.projects.simple.event.store.commands.CheckStreamExists
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.eventserializer.KotlinXSerializationAdapter
import me.xingzhou.projects.simple.event.store.eventsource.InMemoryMapAdapter
import me.xingzhou.projects.simple.event.store.features.SpecificationContext
import me.xingzhou.projects.simple.event.store.features.fixtures.AnEvent
import me.xingzhou.projects.simple.event.store.results.EventStoreResult

class GivenSteps(private val context: SpecificationContext) {
  @Given("the event source system is setup for testing")
  fun theEventSourceSystemIsSetupForTesting() {
    context.adapter = InMemoryMapAdapter()
    context.serializer = KotlinXSerializationAdapter()
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
