package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import org.http4s.{HttpService, Request, StaticFile}
import org.http4s.dsl.Http4sDsl

class FrontendRoute extends Http4sDsl[IO] {
  lazy val service = HttpService[IO] {
    case req @ GET -> "static" /: path =>
      println(s"Getting static file at $path")
      static(path.toString, req)
  }

  private def static(file: String, request: Request[IO]) =
    StaticFile.fromResource(file, Some(request)).getOrElseF(NotFound())

}
