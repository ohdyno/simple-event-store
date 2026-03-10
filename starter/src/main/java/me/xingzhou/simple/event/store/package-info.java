/**
 * Core event-store API centered on {@link me.xingzhou.simple.event.store.EventStore}, with
 * {@link me.xingzhou.simple.event.store.Event} values as the persisted domain messages and
 * {@link me.xingzhou.simple.event.store.EventStoreBuilder} as the default wiring entry point.
 *
 * <p>Main entry points include
 * {@link me.xingzhou.simple.event.store.EventStore#save(me.xingzhou.simple.event.store.Event,
 * me.xingzhou.simple.event.store.entities.Aggregate)},
 * {@link me.xingzhou.simple.event.store.EventStore#enrich(me.xingzhou.simple.event.store.entities.Aggregate)},
 * {@link me.xingzhou.simple.event.store.EventStore#enrich(me.xingzhou.simple.event.store.entities.Projection)},
 * {@link me.xingzhou.simple.event.store.EventStore#publisher()},
 * {@link me.xingzhou.simple.event.store.EventStoreBuilder#build()}, and
 * {@link me.xingzhou.simple.event.store.EventStoreBuilder#buildWithDefaults(java.util.Map)}.
 */
package me.xingzhou.simple.event.store;
