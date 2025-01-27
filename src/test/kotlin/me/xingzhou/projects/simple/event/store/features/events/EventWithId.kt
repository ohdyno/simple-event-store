package me.xingzhou.projects.simple.event.store.features.events

import me.xingzhou.projects.simple.event.store.Event

interface EventWithId : Event {
  val id: String
}
