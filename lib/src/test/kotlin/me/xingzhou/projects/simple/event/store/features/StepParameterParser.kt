package me.xingzhou.projects.simple.event.store.features

import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun String?.asInstant(): Instant {
  val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a z")
  val dateTime = this
  val zonedDateTime = ZonedDateTime.parse(dateTime, formatter)
  return zonedDateTime.toInstant()
}
