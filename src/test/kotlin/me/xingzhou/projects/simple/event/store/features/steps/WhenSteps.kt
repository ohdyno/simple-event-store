package me.xingzhou.projects.simple.event.store.features.steps

import io.cucumber.java.en.When
import me.xingzhou.projects.simple.event.store.EventId
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.commands.AppendToStream
import me.xingzhou.projects.simple.event.store.commands.CreateStream
import me.xingzhou.projects.simple.event.store.commands.ReplayEventsFromStream
import me.xingzhou.projects.simple.event.store.commands.ReplayEventsFromSystem
import me.xingzhou.projects.simple.event.store.commands.RetrieveAppendToken
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromStream
import me.xingzhou.projects.simple.event.store.commands.RetrieveFromSystem
import me.xingzhou.projects.simple.event.store.commands.ValidateAppendToken
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.extensions.handle
import me.xingzhou.projects.simple.event.store.features.SpecificationContext

class WhenSteps(private val context: SpecificationContext) {

  @When(
      "an attempt is made to create a new event stream with the desired stream name and the event")
  @When("creating a stream with this information")
  fun anAttemptIsMadeToCreateANewEventStreamWithTheDesiredStreamNameAndTheEvent() {
    context.result =
        ExecutionContext(
                command =
                    CreateStream(
                        streamName = context.streamName,
                        event = context.event,
                        eventId = EventId(context.event.id),
                        occurredOn = context.occurredOn),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer,
            )
            .let { EventStore().handle(it) }
  }

  @When("appending the event to the stream")
  @When("appending the event to the stream again with the same append token")
  fun appendingTheEventToTheStream() {
    context.result =
        ExecutionContext(
                command =
                    AppendToStream(
                        streamName = context.streamName,
                        event = context.event,
                        eventId = EventId(context.event.id),
                        occurredOn = context.occurredOn,
                        appendToken = context.appendToken),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer,
            )
            .let { EventStore().handle(it) }
  }

  @When("retrieving the append token for the stream")
  fun retrievingTheAppendTokenForTheStream() {
    context.result =
        ExecutionContext(
                command = RetrieveAppendToken(streamName = context.streamName),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .let { EventStore().handle(it) }
  }

  @When("validating the append token for the stream")
  fun validatingTheAppendTokenForTheStream() {
    context.result =
        ExecutionContext(
                command =
                    ValidateAppendToken(
                        streamName = context.streamName, token = context.appendToken),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .let { EventStore().handle(it) }
  }

  @When("retrieving events from the stream")
  fun retrievingEventsFromTheStream() {
    context.result =
        ExecutionContext(
                command =
                    RetrieveFromStream(
                        streamName = context.streamName, eventTypes = context.desiredEventTypes),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .let { EventStore().handle(it) }
  }

  @When("retrieving events from the system")
  fun retrievingEventsFromTheSystem() {
    context.result =
        ExecutionContext(
                command = RetrieveFromSystem(eventTypes = context.desiredEventTypes),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .let { EventStore().handle(it) }
  }

  @When("events are replayed from the stream")
  fun eventsAreReplayedFromTheStream() {
    context.result =
        ExecutionContext(
                command =
                    ReplayEventsFromStream(
                        observerFn = { context.observer }, streamName = context.streamName),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .let { EventStore().handle(it) }
  }

  @When("events are replayed from the system")
  fun eventsAreReplayedFromTheSystem() {
    context.result =
        ExecutionContext(
                command = ReplayEventsFromSystem(observerFn = { context.observer }),
                forEventStorage = context.eventStorage,
                forEventSerialization = context.eventSerializer)
            .let { EventStore().handle(it) }
  }
}
