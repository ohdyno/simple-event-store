package me.xingzhou.projects.simple.event.store.commands

import kotlin.reflect.KType

data class RetrieveFromSystem(val eventTypes: List<KType> = emptyList())
