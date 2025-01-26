package me.xingzhou.projects.simple.event.store.commands

import me.xingzhou.projects.simple.event.store.StreamName

data class CheckStreamExists(val streamName: StreamName)
