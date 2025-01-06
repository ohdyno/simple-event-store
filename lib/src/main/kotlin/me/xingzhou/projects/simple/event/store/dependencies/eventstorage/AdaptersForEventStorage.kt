package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.sql.Connection
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage.Failure.StreamAlreadyExists
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage.Failure.StreamDoesNotExist
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ScriptUtils

fun ForEventStorage(dataSource: DataSource): ForEventStorage {
  return PostgresAdapter(dataSource)
}

fun PostgresAdapter.Companion.setupDatabase(connection: Connection) {
  ClassPathResource("db/schema.sql", PostgresAdapter::class.java.classLoader).let {
    ScriptUtils.executeSqlScript(connection, it)
  }
}

class PostgresAdapter(private val dataSource: DataSource) : ForEventStorage {
  override fun createStream(
      streamName: String,
      eventId: String,
      eventType: String,
      eventData: String,
      occurredOn: Instant
  ): String {
    return dataSource.connection.use {
      it.runCatching {
            prepareStatement(
                    """
                    INSERT INTO eventsource.events
                    (stream_name, version, event_id, event_type, event_data, occurred_on) VALUES
                    (?, ?, ?, ?, (?::jsonb), ?);
                """)
                .apply {
                  setString(1, streamName)
                  setInt(2, 0)
                  setString(3, eventId)
                  setString(4, eventType)
                  setString(5, eventData)
                  setTimestamp(6, Timestamp.from(occurredOn))
                }
                .run { execute() }
                .let { 1.toString() }
          }
          .getOrElse {
            throw when {
              it.isUniqueConstraintViolation() -> StreamAlreadyExists(name = streamName)
              else -> it
            }
          }
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
    return dataSource.connection.use {
      it.runCatching {
            appendToken.toInt().let { version ->
              prepareStatement(
                      "INSERT INTO eventsource.events (stream_name, version, event_id, event_type, event_data, occurred_on) VALUES (?, ?, ?, ?, (?::jsonb), ?);")
                  .apply {
                    setString(1, streamName)
                    setInt(2, version)
                    setString(3, eventId)
                    setString(4, eventType)
                    setString(5, eventData)
                    setTimestamp(6, Timestamp.from(occurredOn))
                  }
                  .run { execute() }
                  .let { version.inc().toString() }
            }
          }
          .getOrElse {
            throw when {
              it.isUniqueConstraintViolation() || it is NumberFormatException ->
                  ForEventStorage.Failure.InvalidAppendToken(
                      streamName = streamName, appendToken = appendToken)
              else -> it
            }
          }
    }
  }

  override fun retrieveFromStream(streamName: String): List<StreamEvent> {
    return dataSource.connection.use {
      it.prepareStatement("SELECT * FROM eventsource.events WHERE stream_name = ?;")
          .apply { setString(1, streamName) }
          .run { executeQuery() }
          .let {
            buildList {
              while (it.next()) {
                add(
                    StreamEvent(
                        eventType = it.getString("event_type"),
                        eventData = it.getString("event_data"),
                        occurredOn = it.getTimestamp("occurred_on").toInstant()))
              }
            }
          }
    }
  }

  override fun retrieveFromSystem(): List<SystemEvent> {
    return dataSource.connection.use {
      it.prepareStatement("SELECT * FROM eventsource.events;")
          .run { executeQuery() }
          .let {
            buildList {
              while (it.next()) {
                add(
                    SystemEvent(
                        streamName = it.getString("stream_name"),
                        streamEvent =
                            StreamEvent(
                                eventType = it.getString("event_type"),
                                eventData = it.getString("event_data"),
                                occurredOn = it.getTimestamp("occurred_on").toInstant())))
              }
            }
          }
    }
  }

  override fun streamExists(streamName: String): Boolean {
    return dataSource.connection.use {
      it.prepareStatement("SELECT 1 FROM eventsource.events WHERE stream_name = ?;")
          .apply { setString(1, streamName) }
          .run { executeQuery() }
          .run { next() }
    }
  }

  override fun retrieveAppendToken(streamName: String): String {
    return dataSource.connection.use {
      it.prepareStatement(
              "SELECT append_token FROM eventsource.append_tokens WHERE stream_name = ?;")
          .apply { setString(1, streamName) }
          .run { executeQuery() }
          .run { if (next()) getString("append_token") else throw StreamDoesNotExist(streamName) }
    }
  }

  override fun validateAppendToken(streamName: String, token: String): Boolean {
    return retrieveAppendToken(streamName) == token
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

  override fun retrieveFromStream(streamName: String): List<StreamEvent> {
    validateStreamExists(streamName)
    return streams[streamName]!!
  }

  override fun retrieveFromSystem(): List<SystemEvent> {
    return streams.flatMap { (streamName, events) ->
      events.map { SystemEvent(streamName = streamName, streamEvent = it) }
    }
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

/*
Private! Utility Extension Functions
 */
private fun Throwable.isUniqueConstraintViolation() =
    this is SQLException && this.sqlState == "23505"
