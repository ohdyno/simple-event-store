package me.xingzhou.projects.simple.event.store.entities;

import java.lang.reflect.Method;
import me.xingzhou.projects.simple.event.store.Event;

public interface EventSourceEntity {
    String APPLY_METHOD_NAME = "apply";

    static boolean isApplyMethod(Method method) {
        return method.getName().equals(APPLY_METHOD_NAME)
                && method.getParameterCount() > 0
                && Event.class.isAssignableFrom(method.getParameterTypes()[0]);
    }
}
