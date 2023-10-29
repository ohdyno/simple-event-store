package me.xingzhou.projects.simple.event.store.features.eventstreams

import io.cucumber.java.en.Given
import me.xingzhou.projects.simple.event.store.InMemoryAdapterForEventSource

class BackgroundStepDefinitions(private val state: FeatureTestState) {
  @Given("the event source is setup for testing")
  fun `the event source is setup for testing`() {
    state.subject = InMemoryAdapterForEventSource()
  }
}
