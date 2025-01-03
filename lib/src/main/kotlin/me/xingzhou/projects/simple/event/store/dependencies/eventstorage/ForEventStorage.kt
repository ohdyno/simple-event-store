package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.time.Instant

interface ForEventStorage {
  fun createStream(
      streamName: String,
      eventName: String,
      eventData: ByteArray,
      occurredOn: Instant
  ): String

  fun appendToStream(
      streamName: String,
      appendToken: String,
      eventName: String,
      eventData: ByteArray,
      occurredOn: Instant
  ): String

  fun retrieveFromStream(streamName: String): List<StreamEvent>

  fun streamExists(streamName: String): Boolean

  fun retrieveAppendToken(streamName: String): String

  fun validateAppendToken(streamName: String, token: String): Boolean

  sealed interface Failure {
    class StreamAlreadyExists(name: String) : Failure, Exception("stream $name is already exists")

    class StreamDoesNotExist(name: String) : Failure, Exception("stream $name does not exist")
  }
}
