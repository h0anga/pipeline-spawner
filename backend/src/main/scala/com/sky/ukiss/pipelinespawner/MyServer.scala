package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import fs2.StreamApp.ExitCode
import io.circe.Json
import fs2.{StreamApp, _}
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.websocket._
import org.http4s.websocket.WebsocketBits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object MyServer extends StreamApp[IO] with Http4sDsl[IO] {
  def myWS(jobEvents: MyJobEvents[IO], scheduler: Scheduler): HttpService[IO] = HttpService[IO] {
    case GET -> Root / "hello" =>
      Ok("Hello world.")

    case GET -> Root / "wsecho" =>
      val toClientScheduled: Stream[IO, WebSocketFrame] =
              scheduler.awakeEvery[IO](1.seconds).map(d => Text(s"Ping! $d"))

      val fromClient: Sink[IO, WebSocketFrame] = _.evalMap { (ws: WebSocketFrame) =>
        IO.apply(println("smth from ws"))
      }

      WebSocketBuilder[IO].build(jobEvents.toClientScheduledFromJobEvents, fromClient)

  }


  val helloService: HttpService[IO] = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, $name")))
  }


  val context = new Context

  def gitHookService= context.gitHookServiceComponent.service
  def stream(args: List[String], requestShutdown: IO[Unit]): Stream[IO, ExitCode] = {
    for {
        scheduler <- Scheduler[IO](corePoolSize = 2)
         exitCode <- BlazeBuilder[IO]
        .bindHttp(8080)
        .withWebSockets(true)
        .mountService(helloService, "/")
        .mountService(gitHookService, "/")
        .mountService(myWS(context.myJobEvents, scheduler), "/")
        .serve
    } yield exitCode
  }

}
