package me.xingzhou.projects.simple.event.store.entities;

import me.xingzhou.projects.simple.event.store.Version;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;

public abstract class BaseAggregate implements Aggregate {
    private long version = EventStorage.Constants.Versions.UNDEFINED_STREAM;

    @Override
    public void setVersion(long version) {
        this.version = version;
    }

    @Override
    public Version version() {
        return new Version(version);
    }
}
