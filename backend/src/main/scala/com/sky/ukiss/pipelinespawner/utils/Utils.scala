package com.sky.ukiss.pipelinespawner.utils

import java.time.{Instant, LocalDateTime, ZoneId}
import java.time.format.DateTimeFormatter

object Utils {
  def formattedTimestamp(instant: Instant): String ={
    val formatter = DateTimeFormatter.ofPattern("yyyMMddHHmmss")
    val ldt = LocalDateTime.ofInstant(instant, ZoneId.of("UTC"))
    ldt.format(formatter)
  }
}
