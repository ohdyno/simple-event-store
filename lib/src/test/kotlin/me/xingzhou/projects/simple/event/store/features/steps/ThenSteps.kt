package me.xingzhou.projects.simple.event.store.features.steps

import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import kotlin.test.fail
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.commands.CheckStreamExists
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromStream
import me.xingzhou.projects.simple.event.store.commands.ValidateAppendToken
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.features.SpecificationContext
import me.xingzhou.projects.simple.event.store.features.fixtures.TypeAEvent
import me.xingzhou.projects.simple.event.store.features.snapshotEventStorage
import me.xingzhou.projects.simple.event.store.results.EventStoreResult
import strikt.api.*
import strikt.assertions.*

class ThenSteps(private val context: SpecificationContext) {

  @Then("the stream was successfully created")
  @Then("the new stream exists in the system")
  fun theStreamWasSuccessfullyCreated() {
    val result =
        ExecutionContext(
                command = CheckStreamExists(streamName = context.streamName),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .run { EventStore().handle(this) } as EventStoreResult.ForCheckStreamExists

    expectThat(result) { get { streamExists }.isTrue() }
  }

  @Then("the system is unchanged")
  fun theSystemIsUnchanged() {
    expectThat(context.snapshotEventStorage()) {
      get { events }.containsExactly(context.eventStorageSnapshot.events)
    }
  }

  @Then("it fails due to a stream with the same name already exists")
  fun itFailsDueToAStreamExistsWithSameName() {
    expectThat(context.result as EventStoreResult.Failure.StreamAlreadyExists) {
      get { streamName }.isEqualTo(context.streamName)
      get { message }.contains("already exists")
    }
  }

  @Then("it fails because the stream does not exist")
  fun itFailsBecauseTheStreamDoesNotExist() {
    expectThat(context.result as EventStoreResult.Failure.StreamDoesNotExist) {
      get { streamName }.isEqualTo(context.streamName)
      get { message }.contains("does not exist")
    }
  }

  @Then("it fails because the append token is invalid")
  fun itFailsBecauseTheAppendTokenIsInvalid() {
    expectThat(context.result as EventStoreResult.Failure.InvalidAppendToken) {
      get { streamName }.isEqualTo(context.streamName)
      get { appendToken }.isEqualTo(context.appendToken)
      get { message }.contains("invalid")
    }
  }

  @And("the stream contains only the event")
  fun theStreamContainsOnlyTheEvent() {
    val (events) =
        ExecutionContext(
                command = RetrieveFromStream(streamName = context.streamName),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .run { EventStore().handle(this) } as EventStoreResult.ForRetrieveFromStream

    expectThat(events).map { it.event }.containsExactly(context.event)
  }

  @Then("the stream contains the new event")
  fun theStreamContainsTheNewEvent() {
    val (events) =
        ExecutionContext(
                command = RetrieveFromStream(streamName = context.streamName),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .run { EventStore().handle(this) } as EventStoreResult.ForRetrieveFromStream

    expectThat(events) { withLast { get { event }.isEqualTo(context.event) } }
  }

  @And("the stream captures when the event occurred")
  fun theStreamCapturesWhenTheEventOccurred() {
    val (events) =
        ExecutionContext(
                command = RetrieveFromStream(streamName = context.streamName),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .run { EventStore().handle(this) } as EventStoreResult.ForRetrieveFromStream

    expectThat(events)
        .filter { it.event == context.event }
        .withLast { get { occurredOn }.isEqualTo(context.occurredOn) }
  }

  @Then("a valid append token for the stream is returned")
  fun anAppendTokenForTheStreamIsReturned() {
    val result =
        (context.result as EventStoreResult.WithAppendToken)
            .run {
              ExecutionContext(
                  command =
                      ValidateAppendToken(streamName = context.streamName, token = appendToken),
                  forEventStorage = context.eventStorage,
                  forEventSerialization = context.eventSerializer)
            }
            .run { EventStore().handle(this) } as EventStoreResult.ForValidateAppendToken

    expectThat(result) { get { appendTokenIsValid }.isTrue() }
  }

  @Then("the events are retrieved in the same order as when they were appended to the stream")
  fun theEventsAreRetrievedInTheSameOrderAsWhenTheyWereAppendedToTheStream() {
    with(context.result as EventStoreResult.ForRetrieveFromStream) {
      expectThat(this.retrievedEvents).isEqualTo(context.expectedStorageContent[context.streamName])
    }
  }

  @Then("only type \"A\" events are retrieved")
  fun onlyTypeAEventsAreRetrieved() {
    with(context.result as EventStoreResult.ForRetrieveFromStream) {
      expectThat(this.retrievedEvents).isNotEmpty().all { get { event }.isA<TypeAEvent>() }
    }
  }

  @Then("both event types are retrieved")
  fun bothEventTypesAreRetrieved() {
    with(context.result) {
      when (this) {
        is EventStoreResult.ForRetrieveFromStream ->
            expectThat(retrievedEvents.map { it.event::class }).all {
              isContainedIn(context.desiredEventTypes)
            }

        is EventStoreResult.ForRetrieveFromSystem ->
            expectThat(events.map { it.event.event::class }).all {
              isContainedIn(context.desiredEventTypes)
            }

        else -> fail("Unexpected result: ${this::class}")
      }
    }
  }

  @Then("no events are retrieved")
  fun noEventsAreRetrieved() {
    with(context.result as EventStoreResult.ForRetrieveFromStream) {
      expectThat(this.retrievedEvents).isEmpty()
    }
  }

  @Then("the events are retrieved in the same order as when they happened")
  fun theEventsAreRetrievedInTheSameOrderAsWhenTheyHappened() {
    with(context.result as EventStoreResult.ForRetrieveFromSystem) {
      expectThat(events).isSorted(Comparator.comparing { it.event.occurredOn })
    }
  }
}
