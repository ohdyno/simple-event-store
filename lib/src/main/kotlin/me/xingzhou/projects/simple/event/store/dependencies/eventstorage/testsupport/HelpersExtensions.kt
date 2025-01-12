package me.xingzhou.projects.simple.event.store.dependencies.eventstorage.testsupport

import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.adapters.inmemory.InMemoryMapAdapter
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.adapters.postgres.EventSourceSql
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.adapters.postgres.PostgresAdapter

fun ForEventStorage.clear() {
  when (this) {
    is PostgresAdapter -> this.deleteAll()
    is InMemoryMapAdapter -> this.deleteAll()
  }
}

private fun PostgresAdapter.deleteAll() =
    dataSource.connection.use { connection ->
      connection.prepareStatement("DELETE FROM ${EventSourceSql.Tables.EVENTS}").run { execute() }
    }

private fun InMemoryMapAdapter.deleteAll() = streams.clear()
