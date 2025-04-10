package me.xingzhou.projects.simple.event.store.entities;

import java.util.List;
import me.xingzhou.projects.simple.event.store.Record;
import me.xingzhou.projects.simple.event.store.StreamName;

public interface Projection extends EventSourceEntity {
    Record lastRecordId();

    List<StreamName> streamNames();
}
