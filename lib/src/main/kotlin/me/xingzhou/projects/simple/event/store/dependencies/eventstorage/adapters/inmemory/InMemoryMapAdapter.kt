package me.xingzhou.projects.simple.event.store.dependencies.eventstorage.adapters.inmemory

import java.time.Instant
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage.Failure.StreamAlreadyExists
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage.Failure.StreamDoesNotExist
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.StreamEvent
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.StreamEvents
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.SystemEvent
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.SystemEvents

fun ForEventStorage(
    streams: MutableMap<String, List<StreamEvent>> = mutableMapOf()
): ForEventStorage {
  return InMemoryMapAdapter(streams)
}

internal class InMemoryMapAdapter(internal val streams: MutableMap<String, List<StreamEvent>>) :
    ForEventStorage {

  override fun createStream(
      streamName: String,
      eventId: String,
      eventType: String,
      eventData: String,
      occurredOn: Instant
  ): String {
    validateStreamDoesNotExist(streamName)
    streams[streamName] =
        listOf(StreamEvent(eventType = eventType, eventData = eventData, occurredOn = occurredOn))
    return retrieveAppendToken(streamName)
  }

  private fun validateStreamDoesNotExist(streamName: String) {
    if (streamExists(streamName)) {
      throw StreamAlreadyExists(streamName)
    }
  }

  override fun appendToStream(
      streamName: String,
      appendToken: String,
      eventId: String,
      eventType: String,
      eventData: String,
      occurredOn: Instant
  ): String {
    validateStreamExists(streamName)
    if (!validateAppendToken(streamName, appendToken)) {
      throw ForEventStorage.Failure.InvalidAppendToken(streamName, appendToken)
    }
    streams[streamName] =
        streams[streamName]!! +
            StreamEvent(eventType = eventType, eventData = eventData, occurredOn = occurredOn)
    return retrieveAppendToken(streamName)
  }

  private fun validateStreamExists(streamName: String) {
    if (!streamExists(streamName)) {
      throw StreamDoesNotExist(streamName)
    }
  }

  override fun retrieveFromStream(streamName: String, eventTypes: List<String>): StreamEvents {
    validateStreamExists(streamName)
    return streams[streamName]!!
        .filter { eventTypes.isEmpty() || it.eventType in eventTypes }
        .let { StreamEvents(events = it, appendToken = retrieveAppendToken(streamName)) }
  }

  override fun retrieveFromSystem(eventTypes: List<String>): SystemEvents {
    return streams
        .flatMap { (streamName, events) ->
          events
              .filter { it.eventType in eventTypes }
              .map { SystemEvent(streamName = streamName, streamEvent = it) }
        }
        .sortedBy { it.streamEvent.occurredOn }
        .let { SystemEvents(events = it) }
  }

  override fun streamExists(streamName: String): Boolean {
    return streams.containsKey(streamName)
  }

  override fun retrieveAppendToken(streamName: String): String {
    validateStreamExists(streamName)
    return streams[streamName]!!.size.dec().toString()
  }

  override fun validateAppendToken(streamName: String, token: String): Boolean {
    return retrieveAppendToken(streamName) == token
  }
}
