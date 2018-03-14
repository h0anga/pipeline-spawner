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
      hook.repository.homepage must_== "http://example.com/mike/diaspora"
    }

    "Converts the payload to a job" >> {
      converter(hook).getKind must_== "Job"
    }
  }
}
