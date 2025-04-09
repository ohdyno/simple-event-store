package me.xingzhou.projects.simple.event.store;

import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.storage.StoredRecord;

public record EventRecord(Event event, RecordDetails details) {
    public static EventRecord extract(StoredRecord record, EventSerializer serializer) {
        var event = serializer.deserialize(record.eventType(), record.eventContent());
        return new EventRecord(
                event, new RecordDetails(record.streamName(), record.version(), record.eventId(), record.timestamp()));
    }
}
