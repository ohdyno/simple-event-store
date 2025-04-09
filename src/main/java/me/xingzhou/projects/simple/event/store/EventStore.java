package me.xingzhou.projects.simple.event.store;

import static me.xingzhou.projects.simple.event.store.internal.tooling.CheckedExceptionHandlers.*;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.xingzhou.projects.simple.event.store.entities.Aggregate;
import me.xingzhou.projects.simple.event.store.failures.StaleStateFailure;
import me.xingzhou.projects.simple.event.store.serializer.EventSerializer;
import me.xingzhou.projects.simple.event.store.storage.EventStorage;
import me.xingzhou.projects.simple.event.store.storage.failures.DuplicateEventStreamFailure;
import me.xingzhou.projects.simple.event.store.storage.failures.StaleVersionFailure;

public class EventStore {
    public static EventStore build(EventStorage storage, EventSerializer serializer) {
        return new EventStore(storage, serializer);
    }

    private final EventStorage storage;

    private final EventSerializer serializer;

    private EventStore(EventStorage storage, EventSerializer serializer) {
        this.storage = storage;
        this.serializer = serializer;
    }

    public Version appendEvent(StreamName streamName, Event event, Version current) {
        var serializedEvent = serializer.serialize(event);
        var record = storage.appendEvent(
                streamName.value(), current.value(), serializedEvent.eventType(), serializedEvent.eventJson());
        return new Version(record.version());
    }

    public Version createStream(StreamName streamName, Event event) {
        var serializedEvent = serializer.serialize(event);
        var record = storage.appendEvent(
                streamName.value(),
                EventStorage.Constants.Versions.UNDEFINED_STREAM,
                serializedEvent.eventType(),
                serializedEvent.eventJson());
        return new Version(record.version());
    }

    public <T extends Aggregate> void enrich(T aggregate) {
        var eventTypes = serializer.extractDefinedEventsFromApplyMethods(aggregate);
        var records = storage.retrieveEvents(
                aggregate.streamName().value(),
                eventTypes,
                aggregate.version().value(),
                EventStorage.Constants.Versions.MAX);
        records.records().stream()
                .map(record -> DeserializedRecord.from(record, serializer))
                .forEach(record -> apply(record, aggregate));
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

    public TimestampedEvents retrieveTimestampedEventsEndingBefore(Instant timestamp) {
        return retrieveTimestampedEvents(null, Collections.emptyList(), Instant.MIN, timestamp);
    }

    public TimestampedEvents retrieveTimestampedEventsStartingAfter(Instant timestamp) {
        return retrieveTimestampedEvents(null, Collections.emptyList(), timestamp, Instant.MAX);
    }

    public VersionedEvents retrieveVersionedEvents(StreamName streamName) {
        return retrieveVersionedEvents(streamName, Collections.emptyList(), Version.start(), Version.end());
    }

    public VersionedEvents retrieveVersionedEvents(StreamName streamName, List<Class<? extends Event>> eventTypes) {
        return retrieveVersionedEvents(streamName, eventTypes, Version.start(), Version.end());
    }

    public VersionedEvents retrieveVersionedEventsEndingBefore(StreamName streamName, Version version) {
        return retrieveVersionedEvents(streamName, Collections.emptyList(), Version.start(), version);
    }

    public VersionedEvents retrieveVersionedEventsStartingAfter(StreamName streamName, Version version) {
        return retrieveVersionedEvents(streamName, Collections.emptyList(), version, Version.end());
    }

    public void save(Event event, Aggregate aggregate) {
        try {
            var serialized = serializer.serialize(event);
            var record = storage.appendEvent(
                    aggregate.streamName().value(),
                    aggregate.version().value(),
                    serialized.eventType(),
                    serialized.eventJson());
            aggregate.setVersion(new Version(record.version()));
        } catch (DuplicateEventStreamFailure | StaleVersionFailure failure) {
            throw new StaleStateFailure();
        }
    }

    private <T extends Aggregate> void apply(DeserializedRecord record, T aggregate) {
        handleExceptions(() -> {
            var lookup = MethodHandles.publicLookup();
            var methodType = MethodType.methodType(void.class, record.event().getClass());
            var applyMethod = lookup.findVirtual(aggregate.getClass(), "apply", methodType);
            applyMethod.invoke(aggregate, record.event());
        });
    }

    private TimestampedEvents retrieveTimestampedEvents(
            StreamName streamName, List<Class<? extends Event>> eventTypes, Instant start, Instant end) {
        var typeNames = eventTypes.stream().map(serializer::getTypeName).toList();
        var stream = Objects.nonNull(streamName) ? streamName.value() : null;
        var timestampedRecords =
                storage.retrieveEvents(start.toEpochMilli(), end.toEpochMilli(), List.of(stream), typeNames);
        var events = timestampedRecords.records().stream()
                .map(record -> DeserializedRecord.from(record, serializer))
                .toList();
        return new TimestampedEvents(events, timestampedRecords.timestamp());
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
}
