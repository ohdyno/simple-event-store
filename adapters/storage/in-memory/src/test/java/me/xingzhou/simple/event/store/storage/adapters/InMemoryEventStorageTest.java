package me.xingzhou.simple.event.store.storage.adapters;

import me.xingzhou.simple.event.store.storage.EventStorage;
import me.xingzhou.simple.event.store.storage.EventStorageTest;

public class InMemoryEventStorageTest extends EventStorageTest {
    @Override
    protected EventStorage createStorage() {
        return new InMemoryEventStorage();
    }
}
