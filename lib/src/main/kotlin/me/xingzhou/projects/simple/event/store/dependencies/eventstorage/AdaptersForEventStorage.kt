package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.time.Instant

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

private class InMemoryMapAdapter(private val streams: MutableMap<String, List<StreamEvent>>) :
    ForEventStorage {

  override fun createStream(streamName: String, eventData: ByteArray, occurredOn: Instant): String {
    streams[streamName] = listOf(StreamEvent(event = eventData, occurredOn = occurredOn))
    return "0"
  }

  override fun retrieveFromStream(streamName: String): List<StreamEvent> = streams[streamName]!!

  override fun streamExists(streamName: String): Boolean {
    return streams.containsKey(streamName)
  }
}
