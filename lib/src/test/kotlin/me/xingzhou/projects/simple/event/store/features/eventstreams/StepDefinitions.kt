package me.xingzhou.projects.simple.event.store.features.eventstreams

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When

class StepDefinitions(private val state: FeatureTestState) {
    @Given("I have an event {string}")
    fun `I have an event`(eventId: String) {

    }

    @When("I create a stream {string} with the event")
    fun `I create a stream with the event`(streamName: String) {

    }

    @Then("I am able to retrieve all the events for {string}")
    fun `I am able to retrieve all the events for`(streamName: String) {

    }

    @And("the only event I received is {string}")
    fun `the only event I received is`(eventId: String) {

    }
}
