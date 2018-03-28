package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import fs2.StreamApp
import io.circe._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends StreamApp[IO] with Http4sDsl[IO] {
  val helloService = HttpService[IO] {
    case GET -> Root / "hello" / name =>
      Ok(Json.obj("message" -> Json.fromString(s"Hello, ${name}")))
  }

  def stream(args: List[String], requestShutdown: IO[Unit]) = {
    val context = new Context
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(helloService, "/")
      .mountService(context.gitHookServiceComponent.service, "/")
      .serve
  }

  def usingCommonClass = Example("foo")

}
