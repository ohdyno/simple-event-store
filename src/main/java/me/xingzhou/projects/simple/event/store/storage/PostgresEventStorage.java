package me.xingzhou.projects.simple.event.store.storage;

import jakarta.annotation.Nonnull;
import java.util.List;
import javax.sql.DataSource;

public class PostgresEventStorage implements EventStorage {
    private final DataSource dataSource;

    public PostgresEventStorage(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Nonnull
    @Override
    public StoredRecord appendEvent(
            @Nonnull String streamName, long currentVersion, @Nonnull String eventType, @Nonnull String eventContent) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Nonnull
    @Override
    public VersionedRecords retrieveEvents(
            @Nonnull String streamName,
            @Nonnull List<String> eventTypes,
            long exclusiveStartVersion,
            long inclusiveEndVersion) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    @Nonnull
    @Override
    public TimestampedRecords retrieveEvents(
            long exclusiveStartId,
            long inclusiveEndId,
            @Nonnull List<String> streamNames,
            @Nonnull List<String> eventTypes) {
        throw new UnsupportedOperationException("Not Implemented");
    }
}
