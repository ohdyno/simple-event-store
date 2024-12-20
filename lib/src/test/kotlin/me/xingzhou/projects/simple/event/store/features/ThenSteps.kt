package me.xingzhou.projects.simple.event.store.features

import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.kotest.matchers.collections.shouldContainExactly
import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.RetrievedEvent
import me.xingzhou.projects.simple.event.store.result.EventStoreResult

class ThenSteps(private val context: SpecificationContext) {

  @Then("the stream was successfully created") fun theStreamWasSuccessfullyCreated() {}

  @And("the stream contains the following events")
  fun theStreamContainsTheFollowingEvents(table: DataTable) {
    val result = context.store.retrieveFromStream(context.streamName)
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
