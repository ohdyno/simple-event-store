package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.time.Instant
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage.Failure.StreamAlreadyExists

fun ForEventStorage(configure: InMemoryMapAdapterBuilder.() -> Unit): ForEventStorage {
  val builder = InMemoryMapAdapterBuilder()
  builder.configure()
  return builder.build()
}

class InMemoryMapAdapterBuilder {
  var streams: MutableMap<String, List<StreamEvent>> = mutableMapOf()

  fun build(): ForEventStorage {
    return InMemoryMapAdapter(streams)
  }
}

internal class InMemoryMapAdapter(internal val streams: MutableMap<String, List<StreamEvent>>) :
    ForEventStorage {

  override fun createStream(streamName: String, eventData: ByteArray, occurredOn: Instant): String {
    if (streams.containsKey(streamName)) {
      throw StreamAlreadyExists(streamName)
    }
    streams[streamName] = listOf(StreamEvent(event = eventData, occurredOn = occurredOn))
    return "0"
  }

  override fun retrieveFromStream(streamName: String): List<StreamEvent> = streams[streamName]!!

  override fun streamExists(streamName: String): Boolean {
    return streams.containsKey(streamName)
  }

  override fun validateAppendToken(streamName: String, token: String): Boolean {
    return streams.size.dec().toString() == token
  }
}
