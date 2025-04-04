package me.xingzhou.projects.simple.event.store.storage;

public class InMemoryEventStorageTests extends EventStorageTests {
    @Override
    protected EventStorage createStorage() {
        return new InMemoryEventStorage();
    }
}
