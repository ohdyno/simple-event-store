package me.xingzhou.projects.simple.event.store.commands

import me.xingzhou.projects.simple.event.store.StreamName

data class RetrieveAppendToken(val streamName: StreamName)
