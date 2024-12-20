package me.xingzhou.projects.simple.event.store.features

import io.cucumber.java.en.When

class WhenSteps(private val context: SpecificationContext) {

  @When(
      "an attempt is made to create a new event stream with the desired stream name and the event")
  fun anAttemptIsMadeToCreateANewEventStreamWithTheDesiredStreamNameAndTheEvent() {
    context.result =
        context.store.createStream(context.streamName, context.event, context.occurredOn)
  }
}
