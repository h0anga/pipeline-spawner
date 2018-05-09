package com.sky.ukiss.pipelinespawner.routes

import com.sky.ukiss.pipelinespawner.KubernetesService
import com.sky.ukiss.pipelinespawner.hooks.GitHookPayload
import org.scalatra.{BadRequest, Ok, ScalatraServlet}

class GitHookServiceComponent(kube: KubernetesService) extends ScalatraServlet {
  import io.circe.generic.auto._

  post("/") {
    io.circe.parser.decode[GitHookPayload](request.body) match {
      case Right(payload) =>
        val submission = kube.onGitHook(payload)
        Ok(s"We extracted the payload and it looks like this: $payload, and the result of the submission is $submission")
      case Left(err) => BadRequest(err)
    }
  }
}
