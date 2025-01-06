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
  val resource = ClassPathResource("db/schema.sql", PostgresAdapter::class.java.classLoader)
  ScriptUtils.executeSqlScript(connection, resource)
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
      try {
        val statement =
            it.prepareStatement(
                """
                    INSERT INTO eventsource.events
                    (stream_name, version, event_id, event_type, event_data, occurred_on) VALUES
                    (?, ?, ?, ?, (?::jsonb), ?);
                """
                    .trimIndent())
        statement.setString(1, streamName)
        statement.setInt(2, 0)
        statement.setString(3, eventId)
        statement.setString(4, eventType)
        statement.setString(5, eventData)
        statement.setTimestamp(6, Timestamp.from(occurredOn))
        statement.execute()
        1.toString()
      } catch (e: SQLException) {
        throw if (e.sqlState == "23505") StreamAlreadyExists(name = streamName) else e
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
      try {
        val statement =
            it.prepareStatement(
                """
                    INSERT INTO eventsource.events
                    (stream_name, version, event_id, event_type, event_data, occurred_on) VALUES
                    (?, ?, ?, ?, (?::jsonb), ?);
                """
                    .trimIndent())
        statement.setString(1, streamName)
        val version =
            appendToken.toIntOrNull()
                ?: throw ForEventStorage.Failure.InvalidAppendToken(
                    streamName = streamName, appendToken = appendToken)
        statement.setInt(2, version)
        statement.setString(3, eventId)
        statement.setString(4, eventType)
        statement.setString(5, eventData)
        statement.setTimestamp(6, Timestamp.from(occurredOn))
        statement.execute()
        version.inc().toString()
      } catch (e: SQLException) {
        throw if (e.sqlState == "23505")
            ForEventStorage.Failure.InvalidAppendToken(
                streamName = streamName, appendToken = appendToken)
        else e
      }
    }
  }

  override fun retrieveFromStream(streamName: String): List<StreamEvent> {
    return dataSource.connection.use {
      val statement =
          it.prepareStatement(
              """
            SELECT * FROM eventsource.events WHERE stream_name = ?;
        """
                  .trimIndent())

      statement.setString(1, streamName)
      val resultSet = statement.executeQuery()
      val events = mutableListOf<StreamEvent>()
      while (resultSet.next()) {
        events.add(
            StreamEvent(
                eventType = resultSet.getString("event_type"),
                eventData = resultSet.getString("event_data"),
                occurredOn = resultSet.getTimestamp("occurred_on").toInstant()))
      }
      events
    }
  }

  override fun retrieveFromSystem(): List<SystemEvent> {
    return dataSource.connection.use {
      val statement =
          it.prepareStatement(
              """
            SELECT * FROM eventsource.events;
        """
                  .trimIndent())

      val resultSet = statement.executeQuery()
      val events = mutableListOf<SystemEvent>()
      while (resultSet.next()) {
        events.add(
            SystemEvent(
                streamName = resultSet.getString("stream_name"),
                streamEvent =
                    StreamEvent(
                        eventType = resultSet.getString("event_type"),
                        eventData = resultSet.getString("event_data"),
                        occurredOn = resultSet.getTimestamp("occurred_on").toInstant())))
      }
      events
    }
  }

  override fun streamExists(streamName: String): Boolean {
    return dataSource.connection.use {
      val statement =
          it.prepareStatement(
              """
              SELECT 1 FROM eventsource.events WHERE stream_name = ?;
          """
                  .trimIndent())
      statement.setString(1, streamName)
      statement.executeQuery().next()
    }
  }

  override fun retrieveAppendToken(streamName: String): String {
    return dataSource.connection.use {
      val statement =
          it.prepareStatement(
              """
              SELECT append_token FROM eventsource.append_tokens WHERE stream_name = ?;
          """
                  .trimIndent())
      statement.setString(1, streamName)
      val resultSet = statement.executeQuery()
      if (resultSet.next()) resultSet.getString("append_token")
      else throw StreamDoesNotExist(streamName)
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
    TODO("Not yet implemented")
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
