package me.xingzhou.projects.simple.event.store

abstract class DomainEvent(val id: EventId) {
  override fun toString(): String {
    return "DomainEvent(id=$id)"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as DomainEvent

    return id == other.id
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}

data class EventId(val eventId: String)
