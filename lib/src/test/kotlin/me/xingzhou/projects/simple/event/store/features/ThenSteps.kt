package me.xingzhou.projects.simple.event.store.features

import io.cucumber.datatable.DataTable
import io.cucumber.java.en.And
import io.cucumber.java.en.Then
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.result.shouldBeSuccess
import me.xingzhou.projects.simple.event.store.DomainEvent
import me.xingzhou.projects.simple.event.store.OccurredOn
import me.xingzhou.projects.simple.event.store.RetrievedEvent
import me.xingzhou.projects.simple.event.store.result.EventStoreResult

class ThenSteps(private val context: SpecificationContext) {

  @Then("the stream was successfully created")
  fun theStreamWasSuccessfullyCreated() {
    context.result!!.shouldBeSuccess()
  }

  @And("the stream contains the following events")
  fun theStreamContainsTheFollowingEvents(table: DataTable) {
    val result = context.store.retrieveFromStream(context.streamName!!)
    val expectedEvents =
        table
            .asMaps()
            .map {
              RetrievedEvent(it["Event Type"].asEvent(), OccurredOn(it["Occurred On"].asInstant()))
            }
            .toList()

    val retrieveResult = result.getOrThrow() as EventStoreResult.ForRetrieveFromStream
    retrieveResult.events.shouldContainExactly(expectedEvents)
  }
}

private fun String?.asEvent(): DomainEvent {
  return when (this) {
    "Type A" -> TypeA()
    else -> throw Error("Unknown Event: $this")
  }
}
