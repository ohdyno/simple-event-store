package me.xingzhou.projects.simple.event.store.features.adapters.eventsource.inmemory

import io.cucumber.java.en.Given
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import me.xingzhou.projects.simple.event.store.Event
import me.xingzhou.projects.simple.event.store.dependencies.eventserializer.ForEventSerializer
import me.xingzhou.projects.simple.event.store.dependencies.eventstorage.ForEventStorage
import me.xingzhou.projects.simple.event.store.features.SpecificationContext
import me.xingzhou.projects.simple.event.store.features.fixtures.TypeAEvent
import me.xingzhou.projects.simple.event.store.features.fixtures.TypeBEvent

class SetupSteps(private val context: SpecificationContext) {
  @Given("the event source system is setup for testing")
  fun theEventSourceSystemIsSetupForTesting() {
    context.eventStorage = ForEventStorage()
    context.eventSerializer = ForEventSerializer {
      serializersModule = SerializersModule {
        polymorphic(Event::class) {
          subclass(TypeAEvent::class)
          subclass(TypeBEvent::class)
        }
      }
    }
  }
}
