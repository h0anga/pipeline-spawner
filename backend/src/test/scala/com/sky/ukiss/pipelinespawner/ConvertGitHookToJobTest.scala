package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.hooks.GitHookPayload
import io.circe.generic.auto._
import org.specs2.mutable.Specification

import scala.collection.JavaConverters
import scala.io.Source

class ConvertGitHookToJobTest extends Specification {
  lazy val payload = Source.fromResource("git-hook.json").mkString
  lazy val hook: GitHookPayload = io.circe.parser.parse(payload).flatMap(_.as[GitHookPayload]).getOrElse(???)

  "The Converter" >> {
    lazy val converter = new ConvertGitHookToJob(() => "id")

    "The payload can be parsed from JSON" >> {
      hook.project.homepage must_== "http://example.com/mike/diaspora"
    }

    "The conversion" >> {
      val conversion = converter(hook)

      "is a Job" >> {
        conversion.getKind must_== "Job"
      }

      "that" >> {
        val container = converter(hook).getSpec.getTemplate.getSpec.getContainers.get(0)
        val commands = container.getCommand.get(2)
        val envVars = JavaConverters.asScalaBuffer(container.getEnv)
        val envVarsNames = envVars.map(_.getName)

        "clones the git repository" >> {
          commands must contain("git clone http://example.com/mike/diaspora.git")
        }

        "checks out the pushed revision" >> {
          commands must contain("git checkout da1560886d4f094c3e6c9ef40349f7d38b5d27d7")
        }

        "has environment variables injected" >> {
          envVarsNames.contains("ARTIFACTORY_USERNAME") must beTrue
          envVarsNames.contains("ARTIFACTORY_PASSWORD") must beTrue
          envVarsNames.contains("GO_PIPELINE_LABEL") must beTrue
        }
      }
    }
  }
}
