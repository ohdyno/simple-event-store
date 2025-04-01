package me.xingzhou.projects.simple.event.store.feature.step.definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import me.xingzhou.projects.simple.event.store.feature.states.TestState;

public class Givens {
  private final TestState state;

  public Givens(TestState state) {
    this.state = state;
  }

  @Given("the event store is operational")
  public void theEventStoreIsOperational() {}

  @And("it has no events")
  public void itHasNoEvents() {}

  @Given("the stream name {string}")
  public void theStreamName(String streamName) {}

  @And("an event")
  public void anEvent() {}
}
