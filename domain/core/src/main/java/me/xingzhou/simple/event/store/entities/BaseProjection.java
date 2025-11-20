package me.xingzhou.simple.event.store.entities;

import java.time.Instant;
import me.xingzhou.simple.event.store.ids.RecordId;
import me.xingzhou.simple.event.store.storage.EventStorage;

public abstract class BaseProjection implements Projection {
    private long lastRecordId = EventStorage.Constants.Ids.UNDEFINED;

    private Instant lastUpdatedOn = EventStorage.Constants.InsertedOnTimestamps.NEVER;
    private boolean enriched = false;

    @Override
    public void handleEnrichedSuccessfully() {
        enriched = true;
    }

    @Override
    public boolean isEnriched() {
        return enriched;
    }

    @Override
    public RecordId lastRecordId() {
        return new RecordId(lastRecordId);
    }

    @Override
    public Instant lastUpdatedOn() {
        return lastUpdatedOn;
    }

    @Override
    public void setLastRecordId(RecordId id) {
        this.lastRecordId = id.id();
    }

    @Override
    public void setLastUpdatedOn(Instant instant) {
        this.lastUpdatedOn = instant;
    }

    /**
     * Defined for Hibernate/ORM purpose.
     *
     * @param enriched the value to set the enriched property to.
     */
    private void setEnriched(boolean enriched) {
        this.enriched = enriched;
    }

    /**
     * Defined for Hibernate/ORM purpose.
     *
     * @param lastRecordId the value to set the lastRecordId property to.
     */
    private void setLastRecordId(long lastRecordId) {
        this.lastRecordId = lastRecordId;
    }
}
