package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.time.Instant
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage.Failure.StreamAlreadyExists

fun ForEventStorage(configure: InMemoryMapAdapterBuilder.() -> Unit): ForEventStorage {
  val builder = InMemoryMapAdapterBuilder()
  builder.configure()
  return builder.build()
}

class InMemoryMapAdapterBuilder {
  private val streams: MutableMap<String, List<StreamEvent>> = mutableMapOf()

  fun build(): ForEventStorage {
    return InMemoryMapAdapter(streams)
  }
}

internal class InMemoryMapAdapter(internal val streams: MutableMap<String, List<StreamEvent>>) :
    ForEventStorage {

  override fun createStream(
      streamName: String,
      eventName: String,
      eventData: ByteArray,
      occurredOn: Instant
  ): String {
    validateStreamDoesNotExist(streamName)
    streams[streamName] =
        listOf(StreamEvent(eventName = eventName, event = eventData, occurredOn = occurredOn))
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
      eventName: String,
      eventData: ByteArray,
      occurredOn: Instant
  ): String {
    validateStreamExists(streamName)
    if (!validateAppendToken(streamName, appendToken)) {
      throw ForEventStorage.Failure.InvalidAppendToken(streamName, appendToken)
    }
    streams[streamName] =
        streams[streamName]!! +
            StreamEvent(eventName = eventName, event = eventData, occurredOn = occurredOn)
    return retrieveAppendToken(streamName)
  }

  private fun validateStreamExists(streamName: String) {
    if (!streamExists(streamName)) {
      throw ForEventStorage.Failure.StreamDoesNotExist(streamName)
    }
  }

  override fun retrieveFromStream(streamName: String): List<StreamEvent> {
    validateStreamExists(streamName)
    return streams[streamName]!!
  }

  override fun streamExists(streamName: String): Boolean {
    return streams.containsKey(streamName)
  }

  override fun retrieveAppendToken(streamName: String): String {
    return retrieveFromStream(streamName).size.dec().toString()
  }

  override fun validateAppendToken(streamName: String, token: String): Boolean {
    return retrieveAppendToken(streamName) == token
  }
}
