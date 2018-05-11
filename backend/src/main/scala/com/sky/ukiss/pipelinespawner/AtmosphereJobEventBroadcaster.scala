package com.sky.ukiss.pipelinespawner

import org.log4s
import org.scalatra.atmosphere.{AtmosphereClient, TextMessage}

class AtmosphereJobEventBroadcaster extends JobEventBroadcaster {
  import scala.concurrent.ExecutionContext.Implicits.global

  private val log = log4s.getLogger

  override def broadcast(event: api.JobEvent): Unit =
    try {
      AtmosphereClient.broadcastAll(TextMessage(event.asJson))
    } catch {
      case e: NoSuchElementException => log.info(s"No client connected (${e.getMessage})")
      case other: Throwable => throw other
    }
}
