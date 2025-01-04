package me.xingzhou.projects.simple.event.store.features

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlin.test.Test
import org.testcontainers.containers.PostgreSQLContainer

class PostgresScaffoldingTest {
  @Test
  fun scaffolding() {
    val container = PostgreSQLContainer<Nothing>("postgres:17.2")
    container.start()

    val hikariConfig = HikariConfig()
    hikariConfig.jdbcUrl = container.jdbcUrl
    hikariConfig.username = container.username
    hikariConfig.password = container.password
    hikariConfig.driverClassName = container.driverClassName

    val ds = HikariDataSource(hikariConfig)
    ds.connection.use { connection -> connection.prepareStatement("SELECT 1").execute() }
  }
}
