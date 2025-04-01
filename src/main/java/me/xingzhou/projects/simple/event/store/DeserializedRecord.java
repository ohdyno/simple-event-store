package me.xingzhou.projects.simple.event.store;

import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.storage.StoredRecord;

public record DeserializedRecord(StreamName streamName, Event event, Version version) {
    public static DeserializedRecord from(StoredRecord record, EventSerializer serializer) {
        var event = serializer.deserialize(record.eventType(), record.eventJson());
        return new DeserializedRecord(new StreamName(record.streamName()), event, new Version(record.version()));
    }
}
