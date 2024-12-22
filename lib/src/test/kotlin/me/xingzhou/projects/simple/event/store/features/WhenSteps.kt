package me.xingzhou.projects.simple.event.store.features

import io.cucumber.java.en.When
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.commands.CreateStream
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext

class WhenSteps(private val context: SpecificationContext) {

  @When(
      "an attempt is made to create a new event stream with the desired stream name and the event")
  @When("creating a stream with this information")
  fun anAttemptIsMadeToCreateANewEventStreamWithTheDesiredStreamNameAndTheEvent() {
    val executionContext =
        ExecutionContext(
            command =
                CreateStream(
                    streamName = context.streamName,
                    event = context.event,
                    occurredOn = context.occurredOn),
            forEventStorage = context.adapter,
            forEventSerialization = context.serializer,
        )
    context.result = EventStore().handle(executionContext)
  }
}
