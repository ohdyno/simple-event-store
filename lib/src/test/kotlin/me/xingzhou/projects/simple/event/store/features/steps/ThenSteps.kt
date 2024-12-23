package me.xingzhou.projects.simple.event.store.features.steps

import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.commands.CheckStreamExists
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromStream
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
            forEventStorage = context.adapter,
            forEventSerialization = context.serializer)
    val result = EventStore().handle(executionContext)

    result as EventStoreResult.ForCheckStreamExists

    result.result shouldBe true
  }

  @And("the stream contains only the event")
  fun theStreamContainsOnlyTheEvent() {
    val executionContext =
        ExecutionContext(
            command = RetrieveFromStream(streamName = context.streamName),
            forEventStorage = context.adapter,
            forEventSerialization = context.serializer)
    val result = EventStore().handle(executionContext)
    val (events) = result as EventStoreResult.ForRetrieveFromStream

    events.map { it.event }.shouldContainExactly(listOf(context.event))
  }

  @And("the stream captures when the event occurred")
  fun theStreamCapturesWhenTheEventOccurred() {
    val executionContext =
        ExecutionContext(
            command = RetrieveFromStream(streamName = context.streamName),
            forEventStorage = context.adapter,
            forEventSerialization = context.serializer)
    val result = EventStore().handle(executionContext)
    val (events) = result as EventStoreResult.ForRetrieveFromStream

    events.map { it.occurredOn }.shouldContainExactly(listOf(context.occurredOn))
  }
}
