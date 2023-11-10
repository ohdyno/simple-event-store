package me.xingzhou.projects.simple.event.store.features

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import me.xingzhou.projects.simple.event.store.*

class GivenSteps(private val context: SpecificationContext) {
  @Given("the event source system is setup for testing")
  fun theEventSourceSystemIsSetupForTesting() {
    val adapter = InMemoryAdapterForEventSource()
    context.adapter = adapter
    context.store = EventStore(adapter)
  }

  @Given("a valid event of type A")
  fun aValidEventOfTypeA() {
    context.event = TypeA()
  }

  @And("^the event occurred on 11/22/2023 12:34:56 PM UTC$")
  fun theEventOccurredOnPMUTC() {
    context.occurredOn = OccurredOn("11/22/2023 12:34:56 PM UTC".asInstant())
  }

  @And("^a desired stream name of \"stream one\"$")
  fun aDesiredStreamNameOfStreamOne() {
    context.streamName = StreamName("stream one")
  }
}
