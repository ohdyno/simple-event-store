package me.xingzhou.projects.simple.event.store.features.steps

import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.commands.CheckStreamExists
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromStream
import me.xingzhou.projects.simple.event.store.commands.ValidateAppendToken
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.features.SpecificationContext
import me.xingzhou.projects.simple.event.store.results.EventStoreResult

class ThenSteps(private val context: SpecificationContext) {

  @Then("the stream was successfully created")
  @Then("the new stream exists in the system")
  fun theStreamWasSuccessfullyCreated() {
    val executionContext =
        ExecutionContext(
            command = CheckStreamExists(streamName = context.streamName),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    val result = EventStore().handle(executionContext)

    result as EventStoreResult.ForCheckStreamExists

    result.result shouldBe true
  }

  @Then("the system is unchanged")
  fun theSystemIsUnchanged() {
    context.snapshotEventStorage() shouldContainExactly context.eventStorageSnapshot
  }

  @Then("it fails due to a stream with the same name already exists")
  fun itFailsDueToAStreamExistsWithSameName() {
    val result = context.result
    result as EventStoreResult.Failure.StreamAlreadyExists
    result.streamName shouldBe context.streamName
    result.message shouldContain "already exists"
  }

  @And("the stream contains only the event")
  fun theStreamContainsOnlyTheEvent() {
    val executionContext =
        ExecutionContext(
            command = RetrieveFromStream(streamName = context.streamName),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    val result = EventStore().handle(executionContext)
    val (events) = result as EventStoreResult.ForRetrieveFromStream

    events.map { it.event }.shouldContainExactly(listOf(context.event))
  }

  @And("the stream captures when the event occurred")
  fun theStreamCapturesWhenTheEventOccurred() {
    val executionContext =
        ExecutionContext(
            command = RetrieveFromStream(streamName = context.streamName),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    val result = EventStore().handle(executionContext)
    val (events) = result as EventStoreResult.ForRetrieveFromStream

    events.map { it.occurredOn }.shouldContainExactly(listOf(context.occurredOn))
  }

  @Then("a valid append token for the stream is returned")
  fun anAppendTokenForTheStreamIsReturned() {
    val result = context.result
    result as EventStoreResult.ForCreateStream

    val executionContext =
        ExecutionContext(
            command =
                ValidateAppendToken(streamName = context.streamName, token = result.appendToken),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
    val validationResult = EventStore().handle(executionContext)

    validationResult as EventStoreResult.ForValidateAppendToken

    validationResult.result shouldBe true
  }
}
