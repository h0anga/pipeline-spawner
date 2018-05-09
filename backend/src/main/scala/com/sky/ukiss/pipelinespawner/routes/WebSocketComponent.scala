package com.sky.ukiss.pipelinespawner.routes

import com.sky.ukiss.pipelinespawner.JobEvents
import org.json4s.{DefaultFormats, Formats}
import org.log4s
import org.scalatra.atmosphere.{AtmosphereSupport, _}
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.scalatra.scalate.ScalateSupport
import org.scalatra.{Ok, ScalatraServlet, SessionSupport}

class WebSocketComponent(jobEvents: JobEvents) extends ScalatraServlet
  with ScalateSupport with JValueResult
  with JacksonJsonSupport with SessionSupport
  with AtmosphereSupport {
  private val log = log4s.getLogger

  get("/hello") {
    Ok("Hello world")
  }

  atmosphere("/") {
    new AtmosphereClient {
      def receive = {
        case Connected =>
        case Disconnected(disconnector, Some(error)) =>
        case Error(Some(error)) => log.error(error.toString)
        case TextMessage(text) => log.warn("Unexpected message from client: " + text)
        case JsonMessage(json) => log.warn("Unexpected message from client: " + json)
      }
    }
  }

  override protected implicit def jsonFormats: Formats = DefaultFormats
}
