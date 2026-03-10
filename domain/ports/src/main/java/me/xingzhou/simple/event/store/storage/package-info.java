/**
 * Storage contracts for appending events to streams and retrieving persisted records.
 *
 * <p>Main entry points include {@link me.xingzhou.simple.event.store.storage.EventStorage#appendEvent(String, long,
 * String, String)}, {@link me.xingzhou.simple.event.store.storage.EventStorage#retrieveEvents(String,
 * java.util.Collection, long, long)}, {@link me.xingzhou.simple.event.store.storage.EventStorage#retrieveEvents(long,
 * long, java.util.Collection, java.util.Collection)}, {@link me.xingzhou.simple.event.store.storage.StoredRecord}, and
 * {@link me.xingzhou.simple.event.store.storage.RetrievedRecords}.
 */
package me.xingzhou.simple.event.store.storage;
