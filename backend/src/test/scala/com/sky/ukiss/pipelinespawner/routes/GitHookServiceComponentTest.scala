package com.sky.ukiss.pipelinespawner.routes

import com.sky.ukiss.pipelinespawner.KubernetesService
import com.sky.ukiss.pipelinespawner.hooks.GithubPayload
import org.scalatra.test.specs2.MutableScalatraSpec
import org.specs2.mock.Mockito
import io.circe.parser.decode
import io.circe.generic.auto._

import scala.io.Source

class GitHookServiceComponentTest extends MutableScalatraSpec with Mockito {
  private val kubeService = mock[KubernetesService]
  addServlet(new GitHookServiceComponent(kubeService), "/hook")

  Seq(
    "github-hook.json",
    "gitlab-hook.json"
  ) foreach { resource =>
    s"POST `$resource` on /hook/git" should {
      val gitHookJson = Source.fromResource(resource).mkString
      val decoded = decode[GithubPayload](gitHookJson).getOrElse(???)

      "return OK and give the hook to the kubernetes service" in {
        post(s"/hook/git", gitHookJson.getBytes) {
          (status must_== 200) and (there was one(kubeService).onGitHook(decoded))
        }
      }
    }
  }
}
