package me.xingzhou.projects.simple.event.store.dependencies.eventstorage.adapters.postgres

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import java.time.Instant
import javax.sql.DataSource
import kotlin.use
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage.Failure.StreamAlreadyExists
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage.Failure.StreamDoesNotExist
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.StreamEvent
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.StreamEvents
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.SystemEvent
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.SystemEvents
import org.springframework.core.io.ClassPathResource
import org.springframework.jdbc.datasource.init.ScriptUtils

fun ForEventStorage(dataSource: DataSource): ForEventStorage {
  return PostgresAdapter(dataSource)
}

fun setupDatabase(connection: Connection) {
  ClassPathResource("db/schema.sql", PostgresAdapter::class.java.classLoader).let {
    ScriptUtils.executeSqlScript(connection, it)
  }
}

internal class PostgresAdapter(internal val dataSource: DataSource) : ForEventStorage {
  override fun createStream(
      streamName: String,
      eventId: String,
      eventType: String,
      eventData: String,
      occurredOn: Instant
  ): String {
    return runCatching {
          dataSource.connection.use { connection ->
            prepareInsertQuery(
                    connection = connection,
                    streamName = streamName,
                    version = 0,
                    eventId = eventId,
                    eventType = eventType,
                    eventData = eventData,
                    occurredOn = occurredOn)
                .run { execute() }
                .let { 1.toString() }
          }
        }
        .getOrElse {
          throw when {
            it.isUniqueConstraintViolation() -> StreamAlreadyExists(name = streamName)
            else -> it
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
    return runCatching {
          dataSource.connection.use { connection ->
            appendToken.toInt().let { version ->
              prepareInsertQuery(
                      connection = connection,
                      streamName = streamName,
                      version = version,
                      eventId = eventId,
                      eventType = eventType,
                      eventData = eventData,
                      occurredOn = occurredOn)
                  .run { execute() }
                  .let { version.inc().toString() }
            }
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

  private fun prepareInsertQuery(
      connection: Connection,
      streamName: String,
      version: Int,
      eventId: String,
      eventType: String,
      eventData: String,
      occurredOn: Instant
  ): PreparedStatement =
      connection.prepareStatement(EventSourceSql.Query.INSERT_EVENT).apply {
        setString(1, streamName)
        setInt(2, version)
        setString(3, eventId)
        setString(4, eventType)
        setString(5, eventData)
        setTimestamp(6, Timestamp.from(occurredOn))
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
      prepareRetrieveQuery(
              connection = connection, streamName = streamName, eventTypes = eventTypes)
          .run { executeQuery() }
          .run { extractStreamEvents() }

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

  override fun retrieveFromSystem(eventTypes: List<String>): SystemEvents {
    return dataSource.connection.use { connection ->
      retrieveSystemTimestamp(connection = connection).let { timestamp ->
        prepareRetrieveQuery(connection = connection, eventTypes = eventTypes)
            .run { executeQuery() }
            .run { extractSystemEvents() }
            .let { SystemEvents(events = it, timestamp = timestamp) }
      }
    }
  }

  private fun retrieveSystemTimestamp(connection: Connection): Instant =
      connection
          .prepareStatement(EventSourceSql.Query.RETRIEVE_SYSTEM_TIMESTAMP)
          .run { executeQuery() }
          .apply { next() }
          .getTimestamp(EventSourceSql.Columns.TIMESTAMP)
          .toInstant()

  private fun prepareRetrieveQuery(
      connection: Connection,
      eventTypes: List<String>
  ): PreparedStatement {
    return when {
      eventTypes.isEmpty() -> {
        connection.prepareStatement(EventSourceSql.Query.RETRIEVE_SYSTEM)
      }
      else -> {
        connection.prepareStatement(EventSourceSql.Query.RETRIEVE_SYSTEM_WITH_FILTER).apply {
          setArray(1, connection.createArrayOf("text", eventTypes.toTypedArray()))
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
            else throw StreamDoesNotExist(name = streamName)
          }

  override fun validateAppendToken(streamName: String, token: String): Boolean {
    return retrieveAppendToken(streamName = streamName) == token
  }
}

internal class EventSourceSql {
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
    const val RETRIEVE_SYSTEM = "SELECT * FROM ${Tables.EVENTS} ORDER BY ${Columns.OCCURRED_ON};"
    const val RETRIEVE_SYSTEM_WITH_FILTER =
        """SELECT * FROM ${Tables.EVENTS}
        WHERE ${Columns.EVENT_TYPE} = ANY (?)
        ORDER BY ${Columns.OCCURRED_ON};"""
    const val STREAM_EXISTS = "SELECT 1 FROM ${Tables.EVENTS} WHERE ${Columns.STREAM_NAME} = ?;"
    const val RETRIEVE_APPEND_TOKEN =
        "SELECT ${Columns.APPEND_TOKEN} FROM ${Tables.APPEND_TOKENS} WHERE ${Columns.STREAM_NAME} = ?;"
    const val RETRIEVE_SYSTEM_TIMESTAMP =
        "SELECT COALESCE(MAX(${Columns.TIMESTAMP}), CURRENT_TIMESTAMP) AS timestamp FROM ${Tables.EVENTS};"
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
    const val TIMESTAMP = "timestamp"
  }
}

/*
Private! Utility Extension Functions
 */
private fun Throwable.isUniqueConstraintViolation() =
    this is SQLException && this.sqlState == "23505"

private fun ResultSet.extractSystemEvents(): List<SystemEvent> = buildList {
  while (next()) {
    add(
        SystemEvent(
            streamName = getString(EventSourceSql.Columns.STREAM_NAME),
            streamEvent =
                StreamEvent(
                    eventType = getString(EventSourceSql.Columns.EVENT_TYPE),
                    eventData = getString(EventSourceSql.Columns.EVENT_DATA),
                    occurredOn = getTimestamp(EventSourceSql.Columns.OCCURRED_ON).toInstant())))
  }
}

private fun ResultSet.extractStreamEvents(): List<StreamEvent> = buildList {
  while (next()) {
    add(
        StreamEvent(
            eventType = getString(EventSourceSql.Columns.EVENT_TYPE),
            eventData = getString(EventSourceSql.Columns.EVENT_DATA),
            occurredOn = getTimestamp(EventSourceSql.Columns.OCCURRED_ON).toInstant()))
  }
}
