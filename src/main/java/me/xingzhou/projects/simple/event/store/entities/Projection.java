package me.xingzhou.projects.simple.event.store.entities;

import java.util.List;
import me.xingzhou.projects.simple.event.store.EventId;
import me.xingzhou.projects.simple.event.store.StreamName;

public interface Projection extends EventSourceEntity {
    EventId lastEventId();

    List<StreamName> streamNames();
}
