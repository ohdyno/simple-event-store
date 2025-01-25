package me.xingzhou.projects.simple.event.store.features.fixtures

class TypeAEventsObserver : EventsRecorder {
  override val observedEvents = mutableListOf<TypeAEvent>()

  fun observe(e: TypeAEvent) {
    observedEvents.add(e)
  }
}
