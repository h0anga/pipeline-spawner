package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import com.sky.ukiss.pipelinespawner.api.{JobEvent, Ping, Pong, WsMessage}
import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import fs2._
import fs2.async.mutable.Topic
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket._
import org.http4s.websocket.WebsocketBits.{Text, WebSocketFrame}
import org.log4s

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationDouble

class WebSocketComponent(jobEventsTopic: Topic[IO, JobEvent]) extends Http4sDsl[IO] {
  private val log = log4s.getLogger

  log.info(s"Created ${getClass.getSimpleName}")

  def service(scheduler: Scheduler): HttpService[IO] = HttpService[IO] {
    case GET -> Root / "hello" =>
      Ok("Hello world.")

    case GET -> Root / "ws" =>
      val pinger: Stream[IO, WsMessage] = scheduler.awakeEvery[IO](1.second)
        .map(_ => WsMessage(Ping))

      val toClient: Stream[IO, WebSocketFrame] = pinger
        .concurrently(jobEventsTopic.subscribe(1).map(event => {println("*** " + event); WsMessage(event)}))
        .map(_.asJson.spaces2)
        .map(s => {println("sending " + s); s})
        .map(s => Text(s, last = true))

      val fromClient: Sink[IO, WebSocketFrame] = _.evalMap { ws: WebSocketFrame =>
        IO {
          log.info("something from WS")
          ws match {
            case Text(t, _) => decode[WsMessage](t) match {
              case Right(WsMessage(Pong)) => ;
              case other => log.error(s"something unexpected from the websocket: $other")
            }
            case _ => log.warn("bad message")
          }
        }
      }

      WebSocketBuilder[IO].build(toClient, fromClient)
  }
}
