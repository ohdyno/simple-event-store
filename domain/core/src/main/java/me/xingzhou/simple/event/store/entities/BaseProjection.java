package me.xingzhou.simple.event.store.entities;

import java.time.Instant;
import me.xingzhou.simple.event.store.ids.RecordId;
import me.xingzhou.simple.event.store.storage.EventStorage;

public abstract class BaseProjection implements Projection {
    private long lastRecordId = EventStorage.Constants.Ids.UNDEFINED;
    private Instant lastUpdatedOn = EventStorage.Constants.InsertedOnTimestamps.NEVER;

    @Override
    public boolean isDefined() {
        return lastUpdatedOn != EventStorage.Constants.InsertedOnTimestamps.NEVER;
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
}
