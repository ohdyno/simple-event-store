package me.xingzhou.projects.simple.event.store.internal.tooling;

import static org.assertj.core.api.Assertions.assertThat;

import me.xingzhou.projects.simple.event.store.Event;
import me.xingzhou.projects.simple.event.store.EventRecord;
import me.xingzhou.projects.simple.event.store.events.HierarchyEventWithInterfaces;
import me.xingzhou.projects.simple.event.store.internal.tooling.recorders.EventInterfaceRecorder;
import org.junit.jupiter.api.Test;

public class EntityEventApplierTest {
    private final Event event = new HierarchyEventWithInterfaces();

    @Test
    void applyEvents() {
        var recorder = new EventInterfaceRecorder();
        var record = new EventRecord(event, null);
        EntityEventApplier.apply(record, recorder);
        assertThat(recorder.wasApplied()).isTrue();
    }
}
