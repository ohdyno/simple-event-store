package me.xingzhou.simple.event.store.enrich;

import me.xingzhou.simple.event.store.Event;
import me.xingzhou.simple.event.store.RecordDetails;
import me.xingzhou.simple.event.store.storage.StoredRecord;

public record EventRecord(Event event, RecordDetails details) {
    public static EventRecord extract(StoredRecord record, Event event) {
        return new EventRecord(
                event, new RecordDetails(record.streamName(), record.version(), record.id(), record.insertedOn()));
    }
}
