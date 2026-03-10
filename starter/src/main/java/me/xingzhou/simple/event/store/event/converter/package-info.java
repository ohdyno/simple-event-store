/**
 * Event type mapping contracts and implementations for translating between persisted event type names and Java event
 * classes.
 *
 * <p>Main entry points include
 * {@link me.xingzhou.simple.event.store.event.converter.EventTypeConverter#convert(Class)},
 * {@link me.xingzhou.simple.event.store.event.converter.EventTypeConverter#convert(String)},
 * {@link me.xingzhou.simple.event.store.event.converter.MapBackedEventTypeConverter}, and
 * {@link me.xingzhou.simple.event.store.event.converter.ServiceLoaderEventTypeConverter}.
 */
package me.xingzhou.simple.event.store.event.converter;
