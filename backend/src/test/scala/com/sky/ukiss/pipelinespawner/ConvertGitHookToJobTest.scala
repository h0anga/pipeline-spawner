package com.sky.ukiss.pipelinespawner

import java.time.{Clock, Instant}

import com.sky.ukiss.pipelinespawner.hooks.GithubPayload
import io.circe.generic.auto._
import io.fabric8.kubernetes.client.DefaultKubernetesClient
import org.mockito.Mockito.when
import org.scalatest.mockito.MockitoSugar
import org.specs2.mutable.Specification

import scala.collection.JavaConverters
import scala.io.Source

class ConvertGitHookToJobTest extends Specification with MockitoSugar {
  val payload = Source.fromResource("github-hook.json").mkString
  val hook: GithubPayload = io.circe.parser.parse(payload).flatMap(_.as[GithubPayload]).getOrElse(???)

  val now = Instant.ofEpochMilli(0)
  val clock = mock[Clock]

  when(clock.instant()) thenReturn now

  "The Converter" >> {
    lazy val converter = new ConvertGitHookToJob(() => "id", clock, new DefaultKubernetesClient())

    "The payload can be parsed from JSON" >> {
      hook.project.get.homepage must_== "http://example.com/mike/diaspora"
    }

    "The conversion" >> {
      val conversion = converter(hook)

      "is a Job" >> {
        conversion.getKind must_== "Job"
      }

      "that" >> {
        val container = converter(hook).getSpec.getTemplate.getSpec.getContainers.get(0)
        val commands = container.getCommand.get(2)
        val envVars = JavaConverters.asScalaBuffer(container.getEnv).map(e => (e.getName, e.getValue)).toMap

        "clones the git repository" >> {
          commands must contain("git clone http://example.com/mike/diaspora.git")
        }

        "checks out the pushed revision" >> {
          commands must contain("git checkout da1560886d4f094c3e6c9ef40349f7d38b5d27d7")
        }

        "has the GO_PIPELINE_LABEL variables injected" >> {
          envVars("GO_PIPELINE_LABEL") must_== "19700101000000"
        }
      }
    }
  }
}
