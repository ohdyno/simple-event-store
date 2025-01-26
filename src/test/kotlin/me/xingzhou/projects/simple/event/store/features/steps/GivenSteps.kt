package me.xingzhou.projects.simple.event.store.features.steps

import io.cucumber.java.en.And
import io.cucumber.java.en.Given
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.reflect.typeOf
import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.EventStore
import me.xingzhou.projects.simple.event.store.OccurredOn
import me.xingzhou.projects.simple.event.store.StreamName
import me.xingzhou.projects.simple.event.store.commands.AppendToStream
import me.xingzhou.projects.simple.event.store.commands.CheckStreamExists
import me.xingzhou.projects.simple.event.store.commands.CreateStream
import me.xingzhou.projects.simple.event.store.commands.RetrieveAppendToken
import me.xingzhou.projects.simple.event.store.dependencies.ExecutionContext
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.testsupport.clear
import me.xingzhou.projects.simple.event.store.features.SpecificationContext
import me.xingzhou.projects.simple.event.store.features.fixtures.*
import me.xingzhou.projects.simple.event.store.features.snapshotEventStorage
import me.xingzhou.projects.simple.event.store.features.store
import me.xingzhou.projects.simple.event.store.results.EventStoreResult
import me.xingzhou.projects.simple.event.store.results.RetrievedEvent
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isFalse

class GivenSteps(private val context: SpecificationContext) {
  @Given("an event")
  @Given("a valid event of type A")
  fun anEvent() {
    context.event = TypeAEvent()
  }

  @And("when the event occurred")
  fun theEventOccurredOnPMUTC() {
    context.occurredOn = OccurredOn(instant = "11/22/2023 12:34:56 PM UTC".asInstant())
  }

  @And("a stream name")
  @And("a stream named \"one\"")
  @And("the stream \"one\"")
  fun aStreamName() {
    context.streamName = StreamName(name = "one")
  }

  @And("a stream named \"two\"")
  fun aStreamNamedTwo() {
    context.streamName = StreamName(name = "two")
  }

  @Given("a new event is appended to the stream")
  fun aNewEventIsAppendedToTheStream() {
    buildList {
          repeat(1) {
            add(
                RetrievedEvent(
                    event = TypeAEvent(id = "${context.streamName.name}-event-$size"),
                    occurredOn =
                        OccurredOn(
                            instant =
                                Instant.now()
                                    .minusSeconds((it * 100).toLong())
                                    .truncatedTo(ChronoUnit.MILLIS))))
          }
        }
        .let {
          ExecutionContext(
                  command = CreateOrAppendToStream(streamName = context.streamName, events = it),
                  forEventStorage = context.eventStorage,
                  forEventSerialization = context.eventSerializer)
              .let { EventStore().handle(context = it) }
        }
        .also { context.store(streamName = context.streamName, events = it) }
  }

  @And("it has many events that are not in chronological order")
  fun itHasManyEventsThatAreNotInChronologicalOrder() {
    buildList {
          repeat(5) {
            add(
                RetrievedEvent(
                    event = TypeAEvent(id = "${context.streamName.name}-event-$size"),
                    occurredOn =
                        OccurredOn(
                            instant =
                                Instant.now()
                                    .minusSeconds((it * 100).toLong())
                                    .truncatedTo(ChronoUnit.MILLIS))))
          }
        }
        .let {
          ExecutionContext(
                  command = CreateOrAppendToStream(streamName = context.streamName, events = it),
                  forEventStorage = context.eventStorage,
                  forEventSerialization = context.eventSerializer)
              .let { EventStore().handle(context = it) }
        }
        .also { context.store(streamName = context.streamName, events = it) }
  }

  @And("it has type \"A\" events")
  @And("it already has many events")
  fun itAlreadyHasManyEvents() {
    buildList {
          repeat(5) {
            add(
                RetrievedEvent(
                    event = TypeAEvent(id = "${context.streamName.name}-event-$size"),
                    occurredOn =
                        OccurredOn(instant = Instant.now().truncatedTo(ChronoUnit.MILLIS))))
          }
        }
        .let {
          ExecutionContext(
                  command = CreateOrAppendToStream(streamName = context.streamName, events = it),
                  forEventStorage = context.eventStorage,
                  forEventSerialization = context.eventSerializer)
              .let { EventStore().handle(context = it) }
        }
        .also { context.store(streamName = context.streamName, events = it) }
  }

  @And("it has type \"B\" events")
  fun itHasTypeBEvents() {
    buildList {
          repeat(5) {
            add(
                RetrievedEvent(
                    event = TypeBEvent(id = "${context.streamName.name}-event-$size"),
                    occurredOn =
                        OccurredOn(instant = Instant.now().truncatedTo(ChronoUnit.MILLIS))))
          }
        }
        .let {
          ExecutionContext(
                  command = CreateOrAppendToStream(streamName = context.streamName, events = it),
                  forEventStorage = context.eventStorage,
                  forEventSerialization = context.eventSerializer)
              .let { EventStore().handle(context = it) }
        }
        .also { context.store(streamName = context.streamName, events = it) }
  }

  @And("it has type \"C\" events")
  fun itHasTypeCEvents() {
    buildList {
          repeat(5) {
            add(
                RetrievedEvent(
                    event = TypeCEvent(id = "${context.streamName.name}-event-$size"),
                    occurredOn =
                        OccurredOn(instant = Instant.now().truncatedTo(ChronoUnit.MILLIS))))
          }
        }
        .let {
          ExecutionContext(
                  command = CreateOrAppendToStream(streamName = context.streamName, events = it),
                  forEventStorage = context.eventStorage,
                  forEventSerialization = context.eventSerializer)
              .let { EventStore().handle(context = it) }
        }
        .also { context.store(streamName = context.streamName, events = it) }
  }

  @And("a new stream name")
  fun aNewStreamName() {
    context.streamName = StreamName(name = "a new stream name")

    ExecutionContext(
            command = CheckStreamExists(streamName = context.streamName),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
        .run { EventStore().handle(context = this) }
        .let { it as EventStoreResult.ForCheckStreamExists }
        .apply { expectThat(streamExists).isFalse() }
  }

  @And("the event already exists in another stream")
  fun theEventAlreadyExistsInAnotherStream() {
    ExecutionContext(
            command =
                CreateStream(
                    streamName = StreamName("stream two"),
                    event = context.event,
                    occurredOn = context.occurredOn),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
        .run { EventStore().handle(context = this) }
  }

  @And("the stream already exists in the system")
  fun theStreamAlreadyExistsInTheSystem() {
    ExecutionContext(
            command =
                CreateStream(
                    streamName = context.streamName,
                    event = TypeAEvent(),
                    occurredOn = OccurredOn(Instant.EPOCH)),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
        .run { EventStore().handle(context = this) }
    context.eventStorageSnapshot = context.snapshotEventStorage()
  }

  @And("a valid append token for the stream")
  fun aValidAppendTokenForTheStream() {
    ExecutionContext(
            command = RetrieveAppendToken(streamName = context.streamName),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer)
        .run { EventStore().handle(context = this) }
        .let { it as EventStoreResult.ForRetrieveAppendToken }
        .run { context.appendToken = appendToken }
  }

  @Given("a stream name for a stream that does not exist")
  fun aStreamNameForAStreamThatDoesNotExist() {
    context.streamName = StreamName(name = "stream that does not exist")
  }

  @And("the append token has been used to append to the stream")
  fun theAppendTokenHasBeenUsedToAppendToTheStream() {
    with(
        ExecutionContext(
            command =
                AppendToStream(
                    streamName = context.streamName,
                    event = context.event,
                    occurredOn = context.occurredOn,
                    appendToken = context.appendToken),
            forEventStorage = context.eventStorage,
            forEventSerialization = context.eventSerializer,
        )) {
          EventStore().handle(context = this)
        }
  }

  @And("an invalid append token")
  @And("any append token")
  fun anInvalidAppendToken() {
    context.appendToken = AppendToken(value = "an invalid append token")
  }

  @And("we only want type \"A\" events")
  @And("we want type \"A\" events")
  fun weOnlyWantTypeAEvents() {
    context.desiredEventTypes.add(typeOf<TypeAEvent>())
  }

  @And("we want type \"B\" events")
  fun weWantTypeBEvents() {
    context.desiredEventTypes.add(typeOf<TypeBEvent>())
  }

  @And("we want type \"C\" events")
  fun weWantTypeCEvents() {
    context.desiredEventTypes.add(typeOf<TypeCEvent>())
  }

  @Given("there are no events in the system")
  fun thereAreNoEventsInTheSystem() {
    context.eventStorage.clear()
  }

  @And("the current system timestamp has been retrieved")
  fun theCurrentSystemTimestampHasBeenRetrieved() {
    context.eventStorageSnapshot = context.snapshotEventStorage()
  }

  @And("it has a stream {string} with {int} events of type {string}")
  fun itHasAStreamWithEvents(streamName: String, numberOfEvents: Int, eventType: String) {
    StreamName(name = streamName).let { streamName ->
      buildList {
            repeat(numberOfEvents) {
              add(
                  RetrievedEvent(
                      event =
                          createEvent(streamName = streamName, size = size, eventType = eventType),
                      occurredOn =
                          OccurredOn(
                              instant =
                                  Instant.now()
                                      .minusSeconds((it * 100).toLong())
                                      .truncatedTo(ChronoUnit.MILLIS))))
            }
          }
          .let {
            ExecutionContext(
                    command = CreateOrAppendToStream(streamName = streamName, events = it),
                    forEventStorage = context.eventStorage,
                    forEventSerialization = context.eventSerializer)
                .let { EventStore().handle(context = it) }
                .apply { expectThat(it) { not().isA<EventStoreResult.Failure>() } }
          }
          .also { context.store(streamName = streamName, events = it) }
    }
  }

  private fun createEvent(streamName: StreamName, size: Int, eventType: String): Event {
    with("${streamName.name}-event-$size") {
      return when (eventType) {
        "A" -> TypeAEvent(id = this)
        "B" -> TypeBEvent(id = this)
        "C" -> TypeCEvent(id = this)
        else -> throw UnsupportedOperationException("unknown event type")
      }
    }
  }

  @Given("an observer")
  @Given("an observer that observes all events")
  fun anObserver() {
    context.observer = AllEventsObserver()
  }

  @Given("an observer that observes only events of type {string}")
  fun anObserverThatObservesOnlyEventsOfType(eventType: String) {
    context.observer =
        when (eventType) {
          "A" -> TypeAEventsObserver()
          else -> throw UnsupportedOperationException("unknown event type for observer")
        }
  }

  @Given("an observer that observes events of type \"A\" and type \"B\"")
  fun anObserverThatObservesEventsOfTypeAndType() {
    context.observer = TypeABEventsObserver()
  }
}

private fun String.asInstant(): Instant =
    DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a z").let {
      ZonedDateTime.parse(/* text= */ this, /* formatter= */ it).toInstant()
    }

private data class CreateOrAppendToStream(
    val streamName: StreamName,
    val events: List<RetrievedEvent>
)

private fun EventStore.handle(context: ExecutionContext<CreateOrAppendToStream>) =
    context.command.events.apply {
      forEach { event ->
        createStream(context = context, event = event).let {
          when {
            it is EventStoreResult.Failure.StreamAlreadyExists -> {
              appendToStream(context = context, event = event)
            }
          }
        }
      }
    }

private fun EventStore.createStream(
    context: ExecutionContext<CreateOrAppendToStream>,
    event: RetrievedEvent
): EventStoreResult =
    with(
        context.copyOf(
            command =
                CreateStream(
                    streamName = context.command.streamName,
                    event = event.event,
                    occurredOn = event.occurredOn))) {
          handle(context = this)
        }

private fun EventStore.appendToStream(
    context: ExecutionContext<CreateOrAppendToStream>,
    event: RetrievedEvent
) =
    with(context.copyOf(command = RetrieveAppendToken(streamName = context.command.streamName))) {
          handle(context = this)
        }
        .run {
          when {
            this is EventStoreResult.ForRetrieveAppendToken -> {
              with(
                  context.copyOf(
                      command =
                          AppendToStream(
                              streamName = context.command.streamName,
                              appendToken = appendToken,
                              event = event.event,
                              occurredOn = event.occurredOn))) {
                    handle(context = this)
                  }
            }

            else -> throw UnsupportedOperationException()
          }
        }
