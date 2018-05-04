package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import org.http4s._
import org.specs2.mock.Mockito
import org.specs2.mutable._

import scala.io.Source

class GitHookEndpointSpec extends Specification with Mockito {
  lazy val hookJson: String = Source.fromResource("git-hook.json").mkString
  lazy val prodConfig = new Context()
  lazy val kubeService = mock[KubernetesService]

  def gitHookEndpoint = new GitHookServiceComponent(kubeService).service

  import cats.effect.IO._

  "Git Hook" >> {
    kubeService.onGitHook(any[GitHookPayload]()) returns "the id of the job"

    "return 200" >> {
      retGitHook.status must_== Status.Ok
    }

    "return the id of the job spawned" >> {
      retGitHook.as[String].unsafeRunSync() must contain("the id of the job")
    }
  }

  private lazy val retGitHook: Response[IO] = {
    val post = Request(Method.POST, Uri.uri("/hook")).withBody(hookJson).unsafeRunSync()
    gitHookEndpoint(post).getOrElse(???).unsafeRunSync()
  }

}
