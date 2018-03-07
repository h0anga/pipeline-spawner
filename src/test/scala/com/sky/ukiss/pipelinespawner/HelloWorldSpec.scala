package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import org.http4s._
import org.mockito.ArgumentMatchers.any
import org.specs2.matcher.MatchResult
import org.specs2.mock.Mockito
import org.specs2.mutable._

import scala.io.Source

class HelloWorldSpec extends Specification with Mockito {
  lazy val hookJson: String = Source.fromResource("git-hook.json").mkString
  lazy val prodConfig = new Configuration()
  lazy val kubeService = mock[KubernetesService].verbose

  def gitHookEndpoint = new GitHookServiceComponent(kubeService).service

  "HelloWorld" >> {
    "return 200" >> {
      uriReturns200()
    }

    "return hello world" >> {
      uriReturnsHelloWorld()
    }
  }

  private[this] val retHelloWorld: Response[IO] = {
    val request = Request[IO](Method.GET, Uri.uri("/hello/world"))
    val response: Response[IO] = Server.helloService(request).getOrElse(???).unsafeRunSync()
    response
  }

  private[this] def uriReturns200(): MatchResult[Status] =
    retHelloWorld.status must beEqualTo(Status.Ok)

  private[this] def uriReturnsHelloWorld(): MatchResult[String] =
    retHelloWorld.as[String].unsafeRunSync() must beEqualTo("{\"message\":\"Hello, world\"}")

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
