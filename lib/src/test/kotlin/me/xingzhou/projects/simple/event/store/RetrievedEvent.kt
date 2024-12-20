package me.xingzhou.projects.simple.event.store

import java.time.Instant

data class RetrievedEvent(val event: ByteArray, val occurredOn: Instant) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RetrievedEvent

        if (!event.contentEquals(other.event)) return false
        if (occurredOn != other.occurredOn) return false

        return true
    }

    override fun hashCode(): Int {
        var result = event.contentHashCode()
        result = 31 * result + occurredOn.hashCode()
        return result
    }
}
