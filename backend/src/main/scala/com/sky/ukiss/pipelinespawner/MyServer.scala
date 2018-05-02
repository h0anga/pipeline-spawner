package com.sky.ukiss.pipelinespawner

import cats.effect.{Effect, IO}
import cats.implicits._
import fs2.StreamApp.ExitCode
import fs2.async.mutable.Queue
import io.circe.Json
import fs2.{StreamApp, _}
import org.http4s.HttpService
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.websocket._
import org.http4s.websocket.WebsocketBits._

import scala.concurrent.ExecutionContext.Implicits.global

object MyServer extends MyServerApp[IO]

class MyServerApp[F[_]](implicit F: Effect[F]) extends StreamApp[F] with Http4sDsl[F] {
  def myWS(jobEvents: JobEvents[F]) = HttpService[F] {
    case GET -> Root / "hello" =>
      Ok("Hello world.")

    case GET -> Root / "wsecho" =>
      val queue: F[Queue[F, WebSocketFrame]] = async.unboundedQueue[F, WebSocketFrame]
      val echoReply: Pipe[F, WebSocketFrame, WebSocketFrame] = _.collect {
        case Text(msg, _) => Text("You sent the server: " + msg)
        case _ => Text("Something new")
      }


//      def echo(e: String): Pipe[F, WebSocketFrame, WebSocketFrame] = _.collect {
//        jobEvents.pushJobInfo()
//      }

//      jobEvents.addQueue(queue)

      val fromClient: Sink[F, WebSocketFrame] = _.evalMap { (ws: WebSocketFrame) =>
        F.delay(println("smth from ws"))
      }

      queue.flatMap { q =>
        jobEvents.addQueue(q)
        val toClient = q.dequeue//.through(echoReply)
//        val e = q.enqueue
        WebSocketBuilder[F].build(toClient, fromClient)
      }

//      WebSocketBuilder[F].build(queue.flatMap(q => q.dequeue), _)

  }


  val helloService = HttpService[F] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, $name")))
  }


//  val context = new Context

//  def gitHookService[F[_]: Effect] = context.myGitHookServiceComponent[F]//.service
//  def jobEvents[F[_]: Effect] = context.jobEvents//.service
  def stream(args: List[String], requestShutdown: F[Unit]): Stream[F, ExitCode] = {
    val context = new Context[F]
    for {
         exitCode <- BlazeBuilder[F]
        .bindHttp(8080)
        .withWebSockets(true)
        .mountService(helloService, "/")
//        .mountService(gitHookService, "/")
//        .mountService(jobEventsService, "/")
        .mountService(myWS(context.jobEvents), "/")
//        .mountService(myWS(), "/")
        .serve
    } yield exitCode
  }

}
