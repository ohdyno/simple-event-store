package me.xingzhou.projects.simple.event.store.features.adapters.eventsource.inmemory

import io.cucumber.java.en.Given
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.adapters.inmemory.ForEventStorage
import me.xingzhou.projects.simple.event.store.features.SpecificationContext
import me.xingzhou.projects.simple.event.store.features.adapters.eventserializer.TestEventsSerializer

class SetupSteps(private val context: SpecificationContext) {
  @Given("the event source system is setup for testing")
  fun theEventSourceSystemIsSetupForTesting() {
    context.eventStorage = ForEventStorage()
    context.eventSerializer = TestEventsSerializer
  }
}
