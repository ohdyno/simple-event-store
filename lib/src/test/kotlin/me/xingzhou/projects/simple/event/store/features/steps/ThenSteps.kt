package me.xingzhou.projects.simple.event.store.features.steps

import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.commands.CheckStreamExists
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromStream
import me.xingzhou.projects.simple.event.store.commands.ValidateAppendToken
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.features.SpecificationContext
import me.xingzhou.projects.simple.event.store.results.EventStoreResult
import strikt.api.*
import strikt.assertions.*

class ThenSteps(private val context: SpecificationContext) {

  @Then("the stream was successfully created")
  @Then("the new stream exists in the system")
  fun theStreamWasSuccessfullyCreated() {
    val executionContext =
        ExecutionContext(
            command = CheckStreamExists(streamName = context.streamName),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    val result = EventStore().handle(executionContext) as EventStoreResult.ForCheckStreamExists

    expectThat(result.result).isTrue()
  }

  @Then("the system is unchanged")
  fun theSystemIsUnchanged() {
    expectThat(context.snapshotEventStorage()) {
      get { entries }.containsExactly(context.eventStorageSnapshot.entries)
    }
  }

  @Then("it fails due to a stream with the same name already exists")
  fun itFailsDueToAStreamExistsWithSameName() {
    val result = context.result as EventStoreResult.Failure.StreamAlreadyExists
    expectThat(result) {
      get { streamName }.isEqualTo(context.streamName)
      get { message }.contains("already exists")
    }
  }

  @Then("it fails because the stream does not exist")
  fun itFailsBecauseTheStreamDoesNotExist() {
    val result = context.result as EventStoreResult.Failure.StreamDoesNotExist
    expectThat(result) {
      get { streamName }.isEqualTo(context.streamName)
      get { message }.contains("does not exist")
    }
  }

  @Then("it fails because the append token is invalid")
  fun itFailsBecauseTheAppendTokenIsInvalid() {
    val result = context.result as EventStoreResult.Failure.InvalidAppendToken
    expectThat(result) {
      get { streamName }.isEqualTo(context.streamName)
      get { appendToken }.isEqualTo(context.appendToken)
      get { message }.contains("invalid")
    }
  }

  @And("the stream contains only the event")
  fun theStreamContainsOnlyTheEvent() {
    val executionContext =
        ExecutionContext(
            command = RetrieveFromStream(streamName = context.streamName),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    val (events) = EventStore().handle(executionContext) as EventStoreResult.ForRetrieveFromStream

    expectThat(events).map { it.event }.containsExactly(context.event)
  }

  @Then("the stream contains the new event")
  fun theStreamContainsTheNewEvent() {
    val executionContext =
        ExecutionContext(
            command = RetrieveFromStream(streamName = context.streamName),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    val (events) = EventStore().handle(executionContext) as EventStoreResult.ForRetrieveFromStream
    expectThat(events) { withLast { get { event }.isEqualTo(context.event) } }
  }

  @And("the stream captures when the event occurred")
  fun theStreamCapturesWhenTheEventOccurred() {
    val executionContext =
        ExecutionContext(
            command = RetrieveFromStream(streamName = context.streamName),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    val (events) = EventStore().handle(executionContext) as EventStoreResult.ForRetrieveFromStream

    expectThat(events.last { it.event == context.event }) {
      get { occurredOn }.isEqualTo(context.occurredOn)
    }
  }

  @Then("a valid append token for the stream is returned")
  fun anAppendTokenForTheStreamIsReturned() {
    val resultWithAppendToken = context.result as EventStoreResult.WithAppendToken
    val executionContext =
        ExecutionContext(
            command =
                ValidateAppendToken(
                    streamName = context.streamName, token = resultWithAppendToken.appendToken),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    val validationResult =
        EventStore().handle(executionContext) as EventStoreResult.ForValidateAppendToken

    expectThat(validationResult) { get { result }.isTrue() }
  }
}
