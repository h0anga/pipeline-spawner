package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import org.http4s.HttpService
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl

class GitHookServiceComponent(kube: KubernetesService) extends Http4sDsl[IO] {
  import io.circe.generic.auto._

  private implicit val payloadDecoder = jsonOf[IO, GitHookPayload]

  lazy val service = HttpService[IO] {
      case req @ POST -> Root / "hook" =>
        for {
          payload <- req.as[GitHookPayload]
          submission <- kube.onGitHook(payload)
          response <- Ok(s"We extracted the payload and it looks like this: $payload, and the result of the submission is $submission")
        } yield response
    }
}
