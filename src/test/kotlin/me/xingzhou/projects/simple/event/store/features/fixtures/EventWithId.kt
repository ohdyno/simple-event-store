package me.xingzhou.projects.simple.event.store.features.fixtures

import me.xingzhou.projects.simple.event.store.Event

interface EventWithId : Event {
  val id: String
}
