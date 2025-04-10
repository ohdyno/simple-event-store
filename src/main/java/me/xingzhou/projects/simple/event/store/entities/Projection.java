package me.xingzhou.projects.simple.event.store.entities;

import java.time.Instant;
import java.util.List;
import me.xingzhou.projects.simple.event.store.RecordId;
import me.xingzhou.projects.simple.event.store.StreamName;

public interface Projection extends EventSourceEntity {
    RecordId lastRecordId();

    Instant lastUpdatedOn();

    void setLastRecordId(RecordId id);

    void setLastUpdatedOn(Instant instant);

    List<StreamName> streamNames();
}
