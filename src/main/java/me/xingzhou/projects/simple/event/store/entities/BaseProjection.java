package me.xingzhou.projects.simple.event.store.entities;

import java.util.Collections;
import java.util.List;
import me.xingzhou.projects.simple.event.store.Record;
import me.xingzhou.projects.simple.event.store.StreamName;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;

public class BaseProjection implements Projection {
    private long lastRecordId = EventStorage.Constants.Ids.UNDEFINED;

    @Override
    public Record lastRecordId() {
        return new Record(lastRecordId);
    }

    @Override
    public List<StreamName> streamNames() {
        return Collections.emptyList();
    }
}
