package me.xingzhou.simple.event.store.entities;

import java.time.Instant;
import java.util.List;
import me.xingzhou.simple.event.store.RecordId;
import me.xingzhou.simple.event.store.StreamName;

public interface Projection extends EventSourceEntity {
    RecordId lastRecordId();

    Instant lastUpdatedOn();

    void setLastRecordId(RecordId id);

    void setLastUpdatedOn(Instant instant);

    List<StreamName> streamNames();
}
