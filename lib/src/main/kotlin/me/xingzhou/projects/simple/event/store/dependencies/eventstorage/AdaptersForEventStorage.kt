package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.sql.Connection
import java.time.Instant
import javax.sql.DataSource
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage.Failure.StreamAlreadyExists
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ScriptUtils

fun ForEventStorage(dataSource: DataSource): ForEventStorage {
  return PostgresAdapter(dataSource)
}

fun PostgresAdapter.Companion.setupDatabase(connection: Connection) {
  val resource = ClassPathResource("db/schema.sql", PostgresAdapter::class.java.classLoader)
  ScriptUtils.executeSqlScript(connection, resource)
}

class PostgresAdapter(private val dataSource: DataSource) : ForEventStorage {
  override fun createStream(
      streamName: String,
      eventType: String,
      eventData: ByteArray,
      occurredOn: Instant
  ): String {
    TODO("Not yet implemented")
  }

  override fun appendToStream(
      streamName: String,
      appendToken: String,
      eventType: String,
      eventData: ByteArray,
      occurredOn: Instant
  ): String {
    TODO("Not yet implemented")
  }

  override fun retrieveFromStream(streamName: String): List<StreamEvent> {
    TODO("Not yet implemented")
  }

  override fun streamExists(streamName: String): Boolean {
    TODO("Not yet implemented")
  }

  override fun retrieveAppendToken(streamName: String): String {
    TODO("Not yet implemented")
  }

  override fun validateAppendToken(streamName: String, token: String): Boolean {
    TODO("Not yet implemented")
  }

  companion object
}

fun ForEventStorage(
    streams: MutableMap<String, List<StreamEvent>> = mutableMapOf()
): ForEventStorage {
  return InMemoryMapAdapter(streams)
}

internal class InMemoryMapAdapter(internal val streams: MutableMap<String, List<StreamEvent>>) :
    ForEventStorage {

  override fun createStream(
      streamName: String,
      eventType: String,
      eventData: ByteArray,
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
      eventType: String,
      eventData: ByteArray,
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
