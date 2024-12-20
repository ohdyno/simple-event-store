package me.xingzhou.projects.simple.event.store.features

import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.kotest.matchers.collections.shouldContainExactly
import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromStream
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.results.EventStoreResult
import me.xingzhou.projects.simple.event.store.results.RetrievedEvent

class ThenSteps(private val context: SpecificationContext) {

  @Then("the stream was successfully created") fun theStreamWasSuccessfullyCreated() {}

  @And("the stream contains the following events")
  fun theStreamContainsTheFollowingEvents(table: DataTable) {
    val executionContext =
        ExecutionContext(
            command = RetrieveFromStream(streamName = context.streamName),
            forEventStorage = context.adapter,
            forEventSerialization = context.serializer)
    val result = EventStore().handle(executionContext)
    val expectedEvents =
        table
            .asMaps()
            .map {
              RetrievedEvent(
                  context.serializer.serialize(it["Event Type"]!!.asEvent()),
                  it["Occurred On"]!!.asInstant())
            }
            .toList()

    result as EventStoreResult.ForRetrieveFromStream

    result.events.shouldContainExactly(expectedEvents)
  }
}

private fun String.asEvent(): Event {
  return when (this) {
    "Type A" -> TypeA()
    else -> throw Error("Unknown Event: $this")
  }
}
