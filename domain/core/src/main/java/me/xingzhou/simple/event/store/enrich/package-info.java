/**
 * Support types used by {@link me.xingzhou.simple.event.store.EventStore} to discover which events an entity can
 * consume and to apply replayed records to that entity.
 *
 * <p>Main entry points include
 * {@link me.xingzhou.simple.event.store.enrich.EntityEventApplier#apply(me.xingzhou.simple.event.store.enrich.EventRecord,
 * me.xingzhou.simple.event.store.entities.EventSourceEntity)},
 * {@link me.xingzhou.simple.event.store.enrich.EventNamesExtractor#extract(me.xingzhou.simple.event.store.entities.EventSourceEntity)},
 * and {@link me.xingzhou.simple.event.store.enrich.EventRecord}.
 */
package me.xingzhou.simple.event.store.enrich;
