package me.xingzhou.simple.event.store.feature.step.definitions;

import io.cucumber.java.en.When;
import me.xingzhou.simple.event.store.feature.states.TestState;

public class Whens {
    private final TestState state;

    public Whens(TestState state) {
        this.state = state;
    }

    @When("creating the stream with the event")
    public void creatingTheStreamWithTheEvent() {}
}
