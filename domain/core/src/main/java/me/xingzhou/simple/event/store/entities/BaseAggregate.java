package me.xingzhou.simple.event.store.entities;

import me.xingzhou.simple.event.store.ids.Version;
import me.xingzhou.simple.event.store.storage.EventStorage;

public abstract class BaseAggregate implements Aggregate {
    private long version = EventStorage.Constants.Versions.UNDEFINED_STREAM;

    private boolean enriched = false;

    @Override
    public void handleEnrichedSuccessfully() {
        setEnriched(true);
    }

    @Override
    public boolean isEnriched() {
        return enriched;
    }

    @Override
    public void setVersion(Version version) {
        this.version = version.value();
    }

    @Override
    public Version version() {
        return new Version(version);
    }

    /**
     * Defined for Hibernate/ORM purpose.
     *
     * @param enriched the value to set the enriched property to.
     */
    private void setEnriched(boolean enriched) {
        this.enriched = enriched;
    }
}
