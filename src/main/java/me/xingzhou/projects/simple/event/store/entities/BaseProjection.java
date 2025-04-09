package me.xingzhou.projects.simple.event.store.entities;

import java.util.Collections;
import java.util.List;
import me.xingzhou.projects.simple.event.store.EventId;
import me.xingzhou.projects.simple.event.store.StreamName;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;

public class BaseProjection implements Projection {
    private long lastEventId = EventStorage.Constants.Ids.UNDEFINED;

    @Override
    public EventId lastEventId() {
        return new EventId(lastEventId);
    }

    @Override
    public List<StreamName> streamNames() {
        return Collections.emptyList();
    }
}
