package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.time.Instant

interface ForEventStorage {
  fun createStream(
      streamName: String,
      eventId: String,
      eventType: String,
      eventData: String,
      occurredOn: Instant
  ): String

  fun appendToStream(
      streamName: String,
      appendToken: String,
      eventId: String,
      eventType: String,
      eventData: String,
      occurredOn: Instant
  ): String

  fun retrieveFromStream(streamName: String, eventTypes: List<String>): List<StreamEvent>

  fun retrieveFromSystem(): List<SystemEvent>

  fun streamExists(streamName: String): Boolean

  fun retrieveAppendToken(streamName: String): String

  fun validateAppendToken(streamName: String, token: String): Boolean

  sealed class Failure(message: String) : Exception(message) {
    class StreamAlreadyExists(name: String) : Failure("stream $name is already exists")

    class StreamDoesNotExist(name: String) : Failure("stream $name does not exist")

    class InvalidAppendToken(streamName: String, appendToken: String) :
        Failure("The append token $appendToken is invalid for stream $streamName")
  }
}
