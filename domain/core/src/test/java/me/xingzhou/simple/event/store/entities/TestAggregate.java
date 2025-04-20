package me.xingzhou.simple.event.store.entities;

import java.util.ArrayList;
import java.util.List;
import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.events.TestEvent;
import me.xingzhou.simple.event.store.ids.StreamName;

public class TestAggregate extends BaseAggregate {
    private final String id = "test-aggregate-id";
    private final List<Event> appliedEvents = new ArrayList<>();

    public List<Event> appliedEvents() {
        return appliedEvents;
    }

    public void apply(TestEvent event) {
        appliedEvents.add(event);
    }

    @Override
    public StreamName streamName() {
        return new StreamName(id);
    }

    @Override
    public String toString() {
        return "TestAggregate{" + "id='" + id + '\'' + ", appliedEvents=" + appliedEvents + '}';
    }
}
