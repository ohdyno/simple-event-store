package me.xingzhou.projects.simple.event.store.features.eventstreams

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlin.test.assertEquals
import me.xingzhou.projects.simple.event.store.DomainEvent
import me.xingzhou.projects.simple.event.store.EventId
import me.xingzhou.projects.simple.event.store.StreamName

class StepDefinitions(private val state: FeatureTestState) {
  @Given("I have an event {string}")
  fun `I have an event`(eventId: String) {
    state.event = object : DomainEvent(EventId(eventId)) {}
  }

  @When("I create a stream {string} with the event")
  fun `I create a stream with the event`(streamName: String) {
    state.subject.createStream(StreamName(streamName), state.event)
  }

  @Then("I am able to retrieve all the events for {string}")
  fun `I am able to retrieve all the events for`(streamName: String) {
    state.retrievedEvents = state.subject.retrieveEvents(StreamName(streamName))
  }

  @And("the only event I received is {string}")
  fun `the only event I received is`(eventId: String) {
    assertEquals(listOf(state.event), state.retrievedEvents)
  }
}
