package me.xingzhou.projects.simple.event.store.entities;

import java.util.ArrayList;
import java.util.List;
import me.xingzhou.projects.simple.event.store.Event;

public class ProjectionRecorder extends BaseProjection {
    private final List<Event> appliedEvents = new ArrayList<>();

    public List<Event> appliedEvents() {
        return appliedEvents;
    }

    public void apply(Event event) {
        appliedEvents.add(event);
    }
}
