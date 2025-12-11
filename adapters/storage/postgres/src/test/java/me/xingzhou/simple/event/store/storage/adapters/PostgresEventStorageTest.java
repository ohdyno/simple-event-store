package me.xingzhou.simple.event.store.storage.adapters;

import static me.xingzhou.simple.event.store.storage.adapters.internal.CheckedExceptionHandlers.handleExceptions;

import me.xingzhou.simple.event.store.storage.EventStorage;
import me.xingzhou.simple.event.store.storage.EventStorageTest;
import org.junit.jupiter.api.BeforeAll;
import org.postgresql.ds.PGSimpleDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
public class PostgresEventStorageTest extends EventStorageTest {
    @Container
    private static final PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:17-alpine").withInitScript("db/postgres/schema.sql");

    private static PGSimpleDataSource dataSource;

    @BeforeAll
    static void createDataSource() {
        dataSource = new PGSimpleDataSource();
        dataSource.setUrl(postgreSQLContainer.getJdbcUrl());
        dataSource.setDatabaseName(postgreSQLContainer.getDatabaseName());
        dataSource.setUser(postgreSQLContainer.getUsername());
        dataSource.setPassword(postgreSQLContainer.getPassword());
    }

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
