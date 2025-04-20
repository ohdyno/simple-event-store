package me.xingzhou.simple.event.store.entities;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import me.xingzhou.simple.event.store.ids.RecordId;
import me.xingzhou.simple.event.store.ids.StreamName;

public interface Projection extends EventSourceEntity {
    RecordId lastRecordId();

    Instant lastUpdatedOn();

    void setLastRecordId(RecordId id);

    void setLastUpdatedOn(Instant instant);

    default List<StreamName> streamNames() {
        return Collections.emptyList();
    }
}
