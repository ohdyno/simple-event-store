package me.xingzhou.projects.simple.event.store.feature.step.definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import me.xingzhou.projects.simple.event.store.feature.states.TestState;

public class Givens {
    private final TestState state;

    public Givens(TestState state) {
        this.state = state;
    }

    @And("an event")
    public void anEvent() {}

    @And("the event store is empty")
    public void theEventStoreIsEmpty() {}

    @Given("the event store is operational")
    public void theEventStoreIsOperational() {}

    @Given("the stream name {string}")
    public void theStreamName(String streamName) {}
}
