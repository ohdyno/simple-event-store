package me.xingzhou.simple.event.store.storage.adapters;

import me.xingzhou.simple.event.store.storage.EventStorage;
import me.xingzhou.simple.event.store.storage.EventStorageTests;

public class InMemoryEventStorageTests extends EventStorageTests {
    @Override
    protected EventStorage createStorage() {
        return new InMemoryEventStorage();
    }
}
