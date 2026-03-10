/**
 * Aggregate and projection contracts for event-sourced entities that can be enriched by
 * {@link me.xingzhou.simple.event.store.EventStore}.
 *
 * <p>Main entry points include {@link me.xingzhou.simple.event.store.entities.Aggregate},
 * {@link me.xingzhou.simple.event.store.entities.Projection},
 * {@link me.xingzhou.simple.event.store.entities.BaseAggregate},
 * {@link me.xingzhou.simple.event.store.entities.BaseProjection},
 * {@link me.xingzhou.simple.event.store.entities.Aggregate#streamName()},
 * {@link me.xingzhou.simple.event.store.entities.Aggregate#version()}, and
 * {@link me.xingzhou.simple.event.store.entities.Projection#streamNames()}.
 */
package me.xingzhou.simple.event.store.entities;
