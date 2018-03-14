package com.sky.ukiss.pipelinespawner

import io.circe.generic.auto._
import org.specs2.mutable.Specification

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
        val that = converter(hook).getSpec.getTemplate.getSpec.getContainers.get(0).getCommand.get(2)

        "clones the git repository" >> {
          that must contain("git clone http://example.com/mike/diaspora.git")
        }

        "checks out the pushed revision" >> {
          that must contain("git checkout da1560886d4f094c3e6c9ef40349f7d38b5d27d7")
        }
      }
    }
  }
}
