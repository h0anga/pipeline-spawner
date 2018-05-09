package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import com.sky.ukiss.pipelinespawner.routes.FrontendRoute
import org.http4s.{Request, Status, Uri}
import org.specs2.mock.Mockito
import org.specs2.mutable._


class FrontendRouteSpec extends Specification with Mockito {

  def frontendRoute = new FrontendRoute().service

  "The frontend assets" >> {
    "should be accessible through the '/static' route" >> {
      val req = Request[IO](uri = Uri.uri("/static/content/index.html"))
      val resp = frontendRoute(req)
      resp.getOrElse(???).unsafeRunSync().status must_== Status.Ok
    }
  }

}
