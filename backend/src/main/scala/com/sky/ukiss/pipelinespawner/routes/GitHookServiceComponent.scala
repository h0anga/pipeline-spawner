package com.sky.ukiss.pipelinespawner.routes

import com.sky.ukiss.pipelinespawner.KubernetesService
import com.sky.ukiss.pipelinespawner.hooks.GithubPayload
import org.scalatra.{BadRequest, Ok, ScalatraServlet}

class GitHookServiceComponent(kube: KubernetesService) extends ScalatraServlet {

  post("/git") {
    import io.circe.generic.auto._
    io.circe.parser.decode[GithubPayload](request.body) match {
      case Right(payload) =>
        val submission = kube.onGitHook(payload)
        Ok(s"We extracted the payload and it looks like this: $payload, and the result of the submission is $submission")
      case Left(err) =>
        BadRequest(err)
    }
  }
}
