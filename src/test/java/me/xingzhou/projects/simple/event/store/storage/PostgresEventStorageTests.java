package me.xingzhou.projects.simple.event.store.storage;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.handleExceptions;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(
        properties = {"spring.sql.init.mode=always", "spring.sql.init.schema-locations=classpath:db/postgres/schema.sql"
        })
public class PostgresEventStorageTests extends EventStorageTests {
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private DataSource dataSource;

    @Override
    protected EventStorage createStorage() {
        return handleExceptions(() -> {
            try (var connection = dataSource.getConnection()) {
                connection
                        .prepareStatement("DELETE FROM %s".formatted(PostgresEventStorage.Schema.Tables.EVENTS_TABLE))
                        .execute();
            }
            return new PostgresEventStorage(dataSource);
        });
    }
}
