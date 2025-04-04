package me.xingzhou.projects.simple.event.store;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;

public class EventStore {
    private final EventStorage storage;
    private final EventSerializer serializer;

    private EventStore(EventStorage storage, EventSerializer serializer) {
        this.storage = storage;
        this.serializer = serializer;
    }

    public static EventStore build(EventStorage storage, EventSerializer serializer) {
        return new EventStore(storage, serializer);
    }

    public Version createStream(StreamName streamName, Event event) {
        var serializedEvent = serializer.serialize(event);
        var current = storage.createStream(
                streamName.value(), event.id(), serializedEvent.eventType(), serializedEvent.eventJson());
        return new Version(current);
    }

    public Version appendEvent(StreamName streamName, Event event, Version current) {
        var serializedEvent = serializer.serialize(event);
        var next = storage.appendEvent(
                streamName.value(),
                current.value(),
                event.id(),
                serializedEvent.eventType(),
                serializedEvent.eventJson());
        return new Version(next);
    }

    public VersionedEvents retrieveVersionedEvents(StreamName streamName) {
        return retrieveVersionedEvents(streamName, Collections.emptyList(), Version.start(), Version.end());
    }

    public VersionedEvents retrieveVersionedEvents(StreamName streamName, List<Class<? extends Event>> eventTypes) {
        return retrieveVersionedEvents(streamName, eventTypes, Version.start(), Version.end());
    }

    public VersionedEvents retrieveVersionedEventsStartingAfter(StreamName streamName, Version version) {
        return retrieveVersionedEvents(streamName, Collections.emptyList(), version, Version.end());
    }

    public VersionedEvents retrieveVersionedEventsEndingBefore(StreamName streamName, Version version) {
        return retrieveVersionedEvents(streamName, Collections.emptyList(), Version.start(), version);
    }

    private VersionedEvents retrieveVersionedEvents(
            StreamName streamName, List<Class<? extends Event>> eventTypes, Version start, Version end) {
        var typeNames = eventTypes.stream().map(serializer::getTypeName).toList();
        var versionedRecords = storage.retrieveEvents(streamName.value(), typeNames, start.value(), end.value());
        var events = versionedRecords.records().stream()
                .map(record -> DeserializedRecord.from(record, serializer))
                .toList();
        return new VersionedEvents(events, new Version(versionedRecords.version()));
    }

    public TimestampedEvents retrieveTimestampedEvents() {
        return retrieveTimestampedEvents(null, Collections.emptyList(), Instant.MIN, Instant.MAX);
    }

    public TimestampedEvents retrieveTimestampedEvents(List<Class<? extends Event>> eventTypes) {
        return retrieveTimestampedEvents(null, eventTypes, Instant.MIN, Instant.MAX);
    }

    public TimestampedEvents retrieveTimestampedEvents(StreamName streamName, List<Class<? extends Event>> eventTypes) {
        return retrieveTimestampedEvents(streamName, eventTypes, Instant.MIN, Instant.MAX);
    }

    public TimestampedEvents retrieveTimestampedEventsStartingAfter(Instant timestamp) {
        return retrieveTimestampedEvents(null, Collections.emptyList(), timestamp, Instant.MAX);
    }

    public TimestampedEvents retrieveTimestampedEventsEndingBefore(Instant timestamp) {
        return retrieveTimestampedEvents(null, Collections.emptyList(), Instant.MIN, timestamp);
    }

    private TimestampedEvents retrieveTimestampedEvents(
            StreamName streamName, List<Class<? extends Event>> eventTypes, Instant start, Instant end) {
        var typeNames = eventTypes.stream().map(serializer::getTypeName).toList();
        var stream = Objects.nonNull(streamName) ? streamName.value() : null;
        var timestampedRecords = storage.retrieveEvents(stream, typeNames, start, end);
        var events = timestampedRecords.records().stream()
                .map(record -> DeserializedRecord.from(record, serializer))
                .toList();
        return new TimestampedEvents(events, timestampedRecords.timestamp());
    }
}
