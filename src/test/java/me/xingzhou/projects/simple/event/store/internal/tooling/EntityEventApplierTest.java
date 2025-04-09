package me.xingzhou.projects.simple.event.store.internal.tooling;

import static org.assertj.core.api.Assertions.assertThat;

import me.xingzhou.projects.simple.event.store.EventRecord;
import me.xingzhou.projects.simple.event.store.events.TestEvent;
import me.xingzhou.projects.simple.event.store.internal.tooling.recorders.Recorder;
import org.junit.jupiter.api.Test;

public class EntityEventApplierTest {
    @Test
    void applyEvents() {
        var recorder = new Recorder();
        var record = new EventRecord(new TestEvent(), null);
        EntityEventApplier.apply(record, recorder);
        assertThat(recorder.wasApplied()).isTrue();
    }
}
