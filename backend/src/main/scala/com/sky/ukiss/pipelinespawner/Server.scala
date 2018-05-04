package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import fs2.StreamApp
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext.Implicits.global

object Server extends StreamApp[IO] with Http4sDsl[IO] {

  def stream(args: List[String], requestShutdown: IO[Unit]) = {
    val context = new Context
    BlazeBuilder[IO]
      .bindHttp(8080, "0.0.0.0")
      .mountService(context.gitHookServiceComponent.service, "/")
      .mountService(context.frontendRoute.service, "/")
      .serve
  }

}
