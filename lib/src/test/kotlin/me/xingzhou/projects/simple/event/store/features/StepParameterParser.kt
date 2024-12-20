package me.xingzhou.projects.simple.event.store.features

import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun String.asInstant(): Instant {
  val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a z")
  val zonedDateTime = ZonedDateTime.parse(this, formatter)
  return zonedDateTime.toInstant()
}
