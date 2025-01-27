package me.xingzhou.projects.simple.event.store

import java.time.Instant

data class OccurredOn(val instant: Instant) : Comparable<OccurredOn> {
  override fun compareTo(other: OccurredOn): Int = instant.compareTo(other.instant)

  companion object {
    fun now() = OccurredOn(instant = Instant.now())
  }
}
