package com.sky.ukiss.pipelinespawner.routes

import java.io.{BufferedReader, InputStreamReader}

import com.sky.ukiss.pipelinespawner.{JobEvents, LogProvider}
import com.sky.ukiss.pipelinespawner.api.JobCreated
import org.json4s.{DefaultFormats, Formats}
import org.log4s
import org.scalatra.atmosphere.{AtmosphereSupport, _}
import org.scalatra.json.{JValueResult, JacksonJsonSupport}
import org.scalatra.scalate.ScalateSupport
import org.scalatra.{Ok, ScalatraServlet, SessionSupport}

import scala.concurrent.ExecutionContext.Implicits.global

class WebSocketComponent(jobEvents: JobEvents, logProvider: LogProvider) extends ScalatraServlet
  with ScalateSupport with JValueResult
  with JacksonJsonSupport with SessionSupport
  with AtmosphereSupport {
  private val log = log4s.getLogger

  get("/hello") {
    Ok("Hello world")
  }

  atmosphere("/jobs") {
    new AtmosphereClient {
      def receive: PartialFunction[InboundMessage, Unit] = {
        case Connected =>
        case Disconnected(disconnector, Some(error)) =>
        case Error(Some(error)) => log.error(error.toString)
        case TextMessage(text) => sendInitialJobs()
        case JsonMessage(json) => log.warn("Unexpected message from client: " + json)
      }

      private def sendInitialJobs(): Unit = {
        jobEvents.getCurrentJobs.
          map(jobEvent => JobCreated(jobEvent._1, jobEvent._2)).
          map(_.asJson).
          map(TextMessage).
          foreach(send)
      }

    }
  }

  atmosphere("/logs/:jobId") {
    new AtmosphereClient {

      def sendLogsOfJob(jobId: String): Unit = new Thread{
        override def run(): Unit = {
          send(TextMessage(logProvider.podLogs(jobId)))
          new BufferedReader(new InputStreamReader(logProvider.streamLogs(jobId))).lines().forEach(line => {
            println("*** sending " + line)
            send(TextMessage(line))
          })
        }
      }.start()

      override def receive: AtmoReceive = {
        case TextMessage(_) =>   sendLogsOfJob(params("jobId"))
        case _ =>
      }
    }
  }

  override protected implicit def jsonFormats: Formats = DefaultFormats
}
