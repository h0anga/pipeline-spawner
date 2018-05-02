package com.sky.ukiss.pipelinespawner

import cats.effect.{Effect, IO}
import org.http4s.{HttpService, Response}
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl


//object MyGitHookServiceComponent extends MyGitHookServiceComponentApp[IO]

//class MyServerApp[F[_]](implicit F: Effect[F])

class MyGitHookServiceComponent[F[_]: Effect](kube: KubernetesService) extends Http4sDsl[F] {
  import io.circe.generic.auto._

  private implicit val payloadDecoder = jsonOf[F, GitHookPayload]

  lazy val service = ???
//    HttpService[F] {
//        case req @ POST -> Root / "hook" =>
//          for {
//            payload <- req.as[GitHookPayload]
//            submission <- IO { kube.onGitHook(payload)}
//            response <- Ok(s"We extracted the payload and it looks like this: $payload, and the result of the submission is $submission")
//          } yield Response[F] (response)
//        case _ =>
//          for {
//          response <- Ok(s"Some fake response")
//        } yield Response[F] (response)
//      }

}
