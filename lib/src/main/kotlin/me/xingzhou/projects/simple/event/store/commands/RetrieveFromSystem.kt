package me.xingzhou.projects.simple.event.store.commands

import kotlin.reflect.KClass
import me.xingzhou.projects.simple.event.store.Event

data class RetrieveFromSystem(val eventTypes: List<KClass<out Event>> = emptyList())
