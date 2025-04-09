package me.xingzhou.projects.simple.event.store.entities;

import me.xingzhou.projects.simple.event.store.StreamName;
import me.xingzhou.projects.simple.event.store.Version;

public interface Aggregate {
    void setVersion(Version version);

    StreamName streamName();

    Version version();
}
