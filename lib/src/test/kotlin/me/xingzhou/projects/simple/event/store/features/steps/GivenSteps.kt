package me.xingzhou.projects.simple.event.store.features.steps

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.OccurredOn
import me.xingzhou.projects.simple.event.store.StreamName
import me.xingzhou.projects.simple.event.store.commands.AppendToStream
import me.xingzhou.projects.simple.event.store.commands.CheckStreamExists
import me.xingzhou.projects.simple.event.store.commands.CreateStream
import me.xingzhou.projects.simple.event.store.commands.RetrieveAppendToken
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.features.SpecificationContext
import me.xingzhou.projects.simple.event.store.features.fixtures.AnEvent
import me.xingzhou.projects.simple.event.store.results.EventStoreResult
import strikt.api.expectThat
import strikt.assertions.isFalse

class GivenSteps(private val context: SpecificationContext) {
  @Given("an event")
  @Given("a valid event of type A")
  fun anEvent() {
    context.event = AnEvent()
  }

  @And("when the event occurred")
  fun theEventOccurredOnPMUTC() {
    context.occurredOn = OccurredOn("11/22/2023 12:34:56 PM UTC".asInstant())
  }

  @And("a stream name")
  fun aStreamName() {
    context.streamName = StreamName("stream one")
  }

  @And("a new stream name")
  fun aNewStreamName() {
    context.streamName = StreamName("a new stream name")

    val result =
        ExecutionContext(
                command = CheckStreamExists(streamName = context.streamName),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .run { EventStore().handle(this) } as EventStoreResult.ForCheckStreamExists

    expectThat(result) { get { streamExists }.isFalse() }
  }

  @And("the event already exists in another stream")
  fun theEventAlreadyExistsInAnotherStream() {
    ExecutionContext(
            command =
                CreateStream(
                    streamName = StreamName("stream two"),
                    event = context.event,
                    occurredOn = context.occurredOn),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
        .run { EventStore().handle(this) }
  }

  @And("the stream already exists in the system")
  fun theStreamAlreadyExistsInTheSystem() {
    ExecutionContext(
            command =
                CreateStream(
                    streamName = context.streamName,
                    event = AnEvent(),
                    occurredOn = OccurredOn(Instant.EPOCH)),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
        .run { EventStore().handle(this) }
    context.eventStorageSnapshot = context.snapshotEventStorage()
  }

  @And("a valid append token for the stream")
  fun aValidAppendTokenForTheStream() {
    val result =
        ExecutionContext(
                command = RetrieveAppendToken(streamName = context.streamName),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .run { EventStore().handle(this) } as EventStoreResult.ForRetrieveAppendToken

    context.appendToken = result.appendToken
  }

  @Given("a stream name for a stream that does not exist")
  fun aStreamNameForAStreamThatDoesNotExist() {
    context.streamName = StreamName("stream that does not exist")
  }

  @And("the append token has been used to append to the stream")
  fun theAppendTokenHasBeenUsedToAppendToTheStream() {
    ExecutionContext(
            command =
                AppendToStream(
                    streamName = context.streamName,
                    event = context.event,
                    occurredOn = context.occurredOn,
                    appendToken = context.appendToken),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer,
        )
        .run { EventStore().handle(this) }
  }

  @And("an invalid append token")
  @And("any append token")
  fun anInvalidAppendToken() {
    context.appendToken = AppendToken("an invalid append token")
  }
}

private fun String.asInstant(): Instant {
  val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a z")
  val zonedDateTime = ZonedDateTime.parse(this, formatter)
  return zonedDateTime.toInstant()
}
