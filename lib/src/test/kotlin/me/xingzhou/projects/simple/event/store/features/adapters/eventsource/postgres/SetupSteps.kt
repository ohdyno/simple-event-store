package me.xingzhou.projects.simple.event.store.features.adapters.eventsource.postgres

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.cucumber.java.AfterAll
import io.cucumber.java.BeforeAll
import io.cucumber.java.en.Given
import javax.sql.DataSource
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.adapters.postgres.ForEventStorage
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.adapters.postgres.setupDatabase
import me.xingzhou.projects.simple.event.store.features.SpecificationContext
import me.xingzhou.projects.simple.event.store.features.fixtures.TestEventsSerializer
import org.testcontainers.containers.PostgreSQLContainer

private lateinit var container: PostgreSQLContainer<Nothing>
private lateinit var dataSource: DataSource

@Suppress("unused")
@BeforeAll
fun setupPostgreSQLContainer() {
  container = PostgreSQLContainer<Nothing>("postgres:17.2").apply { start() }
  dataSource =
      HikariConfig()
          .apply {
            jdbcUrl = container.jdbcUrl
            username = container.username
            password = container.password
            driverClassName = container.driverClassName
          }
          .let { HikariDataSource(it) }
}

@Suppress("unused")
@AfterAll
fun tearDownPostgreSQLContainer() {
  container.stop()
}

class SetupSteps(private val context: SpecificationContext) {

  @Given("the event source system is setup for testing")
  fun theEventSourceSystemIsSetupForTesting() {
    dataSource.connection.use { setupDatabase(it) }

    context.eventStorage = ForEventStorage(dataSource = dataSource)

    context.eventSerializer = TestEventsSerializer
  }
}
