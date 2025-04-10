package me.xingzhou.projects.simple.event.store.eventsmapper;

import java.util.Map;
import me.xingzhou.projects.simple.event.store.Event;

public interface EventTypeMapper {
    Map<String, Class<? extends Event>> getMapping();
}
