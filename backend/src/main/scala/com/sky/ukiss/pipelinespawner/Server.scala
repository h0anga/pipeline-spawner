package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import fs2.{Scheduler, StreamApp}
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder

object Server extends StreamApp[IO] with Http4sDsl[IO] {
  def stream(args: List[String], requestShutdown: IO[Unit]) = {
    import scala.concurrent.ExecutionContext.Implicits.global

    val context = new Context
    for {
      scheduler <- Scheduler[IO](corePoolSize = 2)
      exitCode <- BlazeBuilder[IO]
        .bindHttp(8080, "0.0.0.0")
        .withWebSockets(true)
        .mountService(context.gitHookServiceComponent.service, "/")
        .mountService(context.frontendRoute.service, "/")
        .mountService(context.webSocketComponent.service(scheduler), "/")
        .serve
    } yield exitCode
  }

}
