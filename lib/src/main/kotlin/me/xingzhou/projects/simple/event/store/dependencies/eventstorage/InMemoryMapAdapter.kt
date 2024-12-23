package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.time.Instant

fun ForEventStorage(configure: InMemoryMapAdapterBuilder.() -> Unit): ForEventStorage {
  val builder = InMemoryMapAdapterBuilder()
  builder.configure()
  return builder.build()
}

class InMemoryMapAdapterBuilder {
  var streams: MutableMap<String, List<EventEntry>> = mutableMapOf()

  fun build(): ForEventStorage {
    return InMemoryMapAdapter(streams)
  }
}

private class InMemoryMapAdapter(private val streams: MutableMap<String, List<EventEntry>>) :
    ForEventStorage {

  override fun createStream(streamName: String, eventData: ByteArray, occurredOn: Instant): String {
    streams[streamName] =
        listOf(EventEntry(streamName = streamName, eventData = eventData, occurredOn = occurredOn))
    return "0"
  }

  override fun retrieveFromStream(streamName: String): List<StreamEvent> {
    return streams[streamName]!!.map {
      StreamEvent(event = it.eventData, occurredOn = it.occurredOn)
    }
  }

  override fun streamExists(streamName: String): Boolean {
    return streams.containsKey(streamName)
  }
}

data class EventEntry(val streamName: String, val eventData: ByteArray, val occurredOn: Instant) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as EventEntry

    if (streamName != other.streamName) return false
    if (!eventData.contentEquals(other.eventData)) return false
    if (occurredOn != other.occurredOn) return false

    return true
  }

  override fun hashCode(): Int {
    var result = streamName.hashCode()
    result = 31 * result + eventData.contentHashCode()
    result = 31 * result + occurredOn.hashCode()
    return result
  }
}
