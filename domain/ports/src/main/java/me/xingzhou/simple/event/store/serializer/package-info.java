/**
 * Event serialization contracts for converting domain events to and from stored payloads.
 *
 * <p>Main entry points include
 * {@link me.xingzhou.simple.event.store.serializer.EventSerializer#serialize(me.xingzhou.simple.event.store.Event)},
 * {@link me.xingzhou.simple.event.store.serializer.EventSerializer#deserialize(String, String)},
 * {@link me.xingzhou.simple.event.store.serializer.SerializedEvent}, and
 * {@link me.xingzhou.simple.event.store.serializer.UnknownEventTypeFailure}.
 */
package me.xingzhou.simple.event.store.serializer;
