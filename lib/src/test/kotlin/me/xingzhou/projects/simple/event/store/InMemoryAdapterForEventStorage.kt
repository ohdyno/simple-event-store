package me.xingzhou.projects.simple.event.store

import java.time.Instant
import me.xingzhou.projects.simple.event.store.dependencies.eventsource.ForEventStorage
import me.xingzhou.projects.simple.event.store.results.RetrievedEvent

class InMemoryAdapterForEventStorage : ForEventStorage {
  private val streams = mutableMapOf<String, List<EventEntry>>()

  override fun createStream(streamName: String, eventData: ByteArray, occurredOn: Instant): String {
    streams[streamName] =
        listOf(EventEntry(streamName = streamName, eventData = eventData, occurredOn = occurredOn))
    return "0"
  }

  override fun retrieveFromStream(streamName: String): List<RetrievedEvent> {
    return streams[streamName]!!.map {
      RetrievedEvent(event = it.eventData, occurredOn = it.occurredOn)
    }
  }
}

private data class EventEntry(
    val streamName: String,
    val eventData: ByteArray,
    val occurredOn: Instant
) {
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
