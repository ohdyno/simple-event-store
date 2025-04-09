package me.xingzhou.projects.simple.event.store.internal.tooling.recorders;

import me.xingzhou.projects.simple.event.store.Event;

public class EventInterfaceRecorder {
    private boolean applied = false;

    public void apply(Event event) {
        applied = true;
    }

    public boolean wasApplied() {
        return applied;
    }
}
