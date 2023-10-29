package me.xingzhou.projects.simple.event.store.features.eventstreams

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import kotlin.test.assertEquals
import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.DomainEvent
import me.xingzhou.projects.simple.event.store.EventId
import me.xingzhou.projects.simple.event.store.StreamName

class StepDefinitions(private val state: FeatureTestState) {
  @Given("I have an event {string}")
  fun `I have an event`(eventId: String) {
    state.event = TestDomainEvent(eventId)
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

  @Given("I have an existing stream {string} with the only event {string}")
  fun iHaveAnExistingStreamWithTheOnlyEvent(streamName: String, eventId: String) {
    state.subject.createStream(StreamName(streamName), TestDomainEvent(eventId))
  }

  @And("I have a new event {string}")
  fun iHaveANewEvent(eventId: String) {
    state.event = TestDomainEvent(eventId)
  }

  @When("I append the new event to the existing stream {string}")
  fun iAppendTheNewEventToTheExistingStream(streamName: String) {
    state.subject.appendToStream(StreamName(streamName), state.event, AppendToken(""))
  }

  @Then("I am able to retrieve the following events for {string}")
  fun iAmAbleToRetrieveTheFollowingEventsFor(streamName: String, eventIds: List<String>) {
    val events = state.subject.retrieveEvents(StreamName(streamName))
    val expected = eventIds.map { TestDomainEvent(it) }
    assertEquals(expected, events)
  }
}

private class TestDomainEvent(eventId: String) : DomainEvent(EventId(eventId))
