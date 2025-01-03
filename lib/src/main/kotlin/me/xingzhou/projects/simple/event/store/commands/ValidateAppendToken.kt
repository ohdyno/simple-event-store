package me.xingzhou.projects.simple.event.store.commands

import me.xingzhou.projects.simple.event.store.AppendToken
import me.xingzhou.projects.simple.event.store.StreamName

data class ValidateAppendToken(val streamName: StreamName, val token: AppendToken)
