package me.xingzhou.projects.simple.event.store;

import java.util.List;

public record VersionedEvents(List<VersionedEvent> events, Version version) {}
