package me.xingzhou.projects.simple.event.store.feature.step.definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import me.xingzhou.projects.simple.event.store.feature.states.TestState;

public class Thens {
    private final TestState state;

    public Thens(TestState state) {
        this.state = state;
    }

    @Then("a stream with the specified name is created")
    public void aStreamWithTheSpecifiedNameIsCreated() {}

    @And("it contains the specified event")
    public void itContainsTheSpecifiedEvent() {}
}
