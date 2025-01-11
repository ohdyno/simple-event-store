package me.xingzhou.projects.simple.event.store.dependencies.eventstorage

import java.sql.Connection
import java.sql.PreparedStatement
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

private class EventSourceSql {
  object Query {
    const val INSERT_EVENT =
        """INSERT INTO ${Tables.EVENTS} (${Columns.STREAM_NAME}, ${Columns.VERSION}, ${Columns.EVENT_ID}, ${Columns.EVENT_TYPE}, ${Columns.EVENT_DATA}, ${Columns.OCCURRED_ON})
           VALUES                       (           ?,                      ?,                  ?,                  ?,                  (?::jsonb),                 ?         );"""
    const val RETRIEVE_STREAM_WITH_FILTER =
        """SELECT * FROM ${Tables.EVENTS} 
           WHERE ${Columns.STREAM_NAME} = ?
           AND ${Columns.EVENT_TYPE} = ANY (?)
           ORDER BY ${Columns.VERSION};"""
    const val RETRIEVE_STREAM =
        """SELECT * FROM ${Tables.EVENTS}
           WHERE ${Columns.STREAM_NAME} = ?
           ORDER BY ${Columns.VERSION};"""
    const val RETRIEVE_SYSTEM = "SELECT * FROM ${Tables.EVENTS};"
    const val STREAM_EXISTS = "SELECT 1 FROM ${Tables.EVENTS} WHERE ${Columns.STREAM_NAME} = ?;"
    const val RETRIEVE_APPEND_TOKEN =
        "SELECT ${Columns.APPEND_TOKEN} FROM ${Tables.APPEND_TOKENS} WHERE ${Columns.STREAM_NAME} = ?;"
  }

  object Tables {
    const val EVENTS = "eventsource.events"
    const val APPEND_TOKENS = "eventsource.append_tokens"
  }

  object Columns {
    const val STREAM_NAME = "stream_name"
    const val VERSION = "version"
    const val EVENT_ID = "event_id"
    const val EVENT_TYPE = "event_type"
    const val EVENT_DATA = "event_data"
    const val OCCURRED_ON = "occurred_on"
    const val APPEND_TOKEN = "append_token"
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
            prepareStatement(EventSourceSql.Query.INSERT_EVENT)
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
              prepareStatement(EventSourceSql.Query.INSERT_EVENT)
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

  override fun retrieveFromStream(streamName: String, eventTypes: List<String>): StreamEvents {
    return dataSource.connection.use { connection ->
      retrieveAppendToken(connection = connection, streamName = streamName).let { appendToken ->
        retrieveFromStream(
                connection = connection, streamName = streamName, eventTypes = eventTypes)
            .let { events -> StreamEvents(events = events, appendToken = appendToken) }
      }
    }
  }

  private fun retrieveFromStream(
      connection: Connection,
      streamName: String,
      eventTypes: List<String>
  ): List<StreamEvent> =
      prepareRetrieveQuery(connection, streamName, eventTypes)
          .run { executeQuery() }
          .let {
            buildList {
              while (it.next()) {
                add(
                    StreamEvent(
                        eventType = it.getString(EventSourceSql.Columns.EVENT_TYPE),
                        eventData = it.getString(EventSourceSql.Columns.EVENT_DATA),
                        occurredOn =
                            it.getTimestamp(EventSourceSql.Columns.OCCURRED_ON).toInstant()))
              }
            }
          }

  private fun prepareRetrieveQuery(
      connection: Connection,
      streamName: String,
      eventTypes: List<String>
  ): PreparedStatement =
      when {
        eventTypes.isEmpty() -> {
          connection.prepareStatement(EventSourceSql.Query.RETRIEVE_STREAM).apply {
            setString(1, streamName)
          }
        }
        else -> {
          connection.prepareStatement(EventSourceSql.Query.RETRIEVE_STREAM_WITH_FILTER).apply {
            setString(1, streamName)
            setArray(2, connection.createArrayOf("text", eventTypes.toTypedArray()))
          }
        }
      }

  override fun retrieveFromSystem(): List<SystemEvent> {
    return dataSource.connection.use {
      it.prepareStatement(EventSourceSql.Query.RETRIEVE_SYSTEM)
          .run { executeQuery() }
          .let {
            buildList {
              while (it.next()) {
                add(
                    SystemEvent(
                        streamName = it.getString(EventSourceSql.Columns.STREAM_NAME),
                        streamEvent =
                            StreamEvent(
                                eventType = it.getString(EventSourceSql.Columns.EVENT_TYPE),
                                eventData = it.getString(EventSourceSql.Columns.EVENT_DATA),
                                occurredOn =
                                    it.getTimestamp(EventSourceSql.Columns.OCCURRED_ON)
                                        .toInstant())))
              }
            }
          }
    }
  }

  override fun streamExists(streamName: String): Boolean {
    return dataSource.connection.use {
      it.prepareStatement(EventSourceSql.Query.STREAM_EXISTS)
          .apply { setString(1, streamName) }
          .run { executeQuery() }
          .run { next() }
    }
  }

  override fun retrieveAppendToken(streamName: String): String {
    return dataSource.connection.use { retrieveAppendToken(it, streamName) }
  }

  private fun retrieveAppendToken(connection: Connection, streamName: String): String =
      connection
          .prepareStatement(EventSourceSql.Query.RETRIEVE_APPEND_TOKEN)
          .apply { setString(1, streamName) }
          .run { executeQuery() }
          .run {
            if (next()) getString(EventSourceSql.Columns.APPEND_TOKEN)
            else throw StreamDoesNotExist(streamName)
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

  override fun retrieveFromStream(streamName: String, eventTypes: List<String>): StreamEvents {
    validateStreamExists(streamName)
    return streams[streamName]!!
        .filter { eventTypes.isEmpty() || it.eventType in eventTypes }
        .let { StreamEvents(events = it, appendToken = retrieveAppendToken(streamName)) }
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
    validateStreamExists(streamName)
    return streams[streamName]!!.size.dec().toString()
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
