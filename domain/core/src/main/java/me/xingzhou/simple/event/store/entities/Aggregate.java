package me.xingzhou.simple.event.store.entities;

import me.xingzhou.simple.event.store.StreamName;
import me.xingzhou.simple.event.store.Version;

public interface Aggregate extends EventSourceEntity {
    void setVersion(Version version);

    StreamName streamName();

    Version version();
}
