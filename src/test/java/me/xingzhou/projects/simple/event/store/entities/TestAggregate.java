package me.xingzhou.projects.simple.event.store.entities;

import me.xingzhou.projects.simple.event.store.StreamName;

public class TestAggregate extends BaseAggregate {
    private final String id = "test-aggregate-id";

    @Override
    public StreamName streamName() {
        return new StreamName(id);
    }
}
