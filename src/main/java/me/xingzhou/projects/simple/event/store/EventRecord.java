package me.xingzhou.projects.simple.event.store;

import me.xingzhou.projects.simple.event.store.storage.StoredRecord;

public record EventRecord(Event event, RecordDetails details) {
    public static EventRecord extract(StoredRecord record, Event event) {
        return new EventRecord(
                event, new RecordDetails(record.streamName(), record.version(), record.id(), record.insertedOn()));
    }
}
