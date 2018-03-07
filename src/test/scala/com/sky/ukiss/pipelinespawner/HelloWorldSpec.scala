package com.sky.ukiss.pipelinespawner

import java.io.{BufferedReader, InputStreamReader}

import cats.effect.IO
import org.http4s._
import org.specs2.matcher.MatchResult

import scala.io.Source

class HelloWorldSpec extends org.specs2.mutable.Specification {
  val hookJson: String = Source.fromResource("/git-hook.json").mkString

  def gitHookEndpoint = new Configuration().gitHookServiceComponent.service

  "HelloWorld" >> {
    "return 200" >> {
      val request = Request[IO](Method.GET, Uri.uri("/hello/world"))
      val response = HelloWorldServer.helloService(request)
      response === """{"message":hello, world"}"""
    }

    "return hello world" >> {
      uriReturnsHelloWorld()
    }
  }

  import fs2.Stream
  import cats.effect.IO._

  "Git Hook" >> {
    "return 200" >> {
      val post = Request(Method.POST, Uri.uri("/hook")).withBody(hookJson).unsafeRunSync()
      val task = gitHookEndpoint.run(post)
      val response = ??? // TODO get the response
    }

    "return the id of the job spawned" >> {

    }
  }

//  private[this] val retHelloWorld: Response[IO] = {
//    val request = Request[IO](Method.GET, Uri.uri("/hello/world"))
//    val responseTask = HelloWorldServer.helloService.run(request)
//    val response: Option[_] = responseTask.run.body.runLast.run
//    response.get.decodeUtf8.right.get shouldEqual """{"message":hello, world"}"""
//  }

  private[this] def uriReturns200(): MatchResult[Status] =
    retHelloWorld.status must beEqualTo(Status.Ok)

  private[this] def uriReturnsHelloWorld(): MatchResult[String] =
    retHelloWorld.as[String].unsafeRunSync() must beEqualTo("{\"message\":\"Hello, world\"}")
}
