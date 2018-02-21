package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import fs2.StreamApp
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import scala.concurrent.ExecutionContext.Implicits.global

object HelloWorldServer extends StreamApp[IO] with Http4sDsl[IO] {
  val service = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
  }

  implicit val payloadDecoder = jsonOf[IO, GitHookPayload]
  val gitHookService = HttpService[IO] {
    case req @ POST -> Root / "hook" => for {
        payload <- req.as[GitHookPayload]
        resp <- Ok(s"We extracted the payload and it looks like this: $payload")
      } yield resp
  }

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(service, "/")
      .mountService(gitHookService, "/")
      .serve
}
