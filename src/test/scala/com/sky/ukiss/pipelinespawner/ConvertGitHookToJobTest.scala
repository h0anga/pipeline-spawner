package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import io.circe.generic.semiauto
import org.specs2.mutable.Specification

import scala.io.Source

class ConvertGitHookToJobTest extends Specification {
  lazy val converter = new ConvertGitHookToJob(() => "id")

  lazy val payloadDecoder = semiauto.deriveDecoder[GitHookPayload]
  lazy val payload = Source.fromResource("git-hook.json").mkString
  lazy val hook: GitHookPayload = io.circe.parser.parse(payload).flatMap(payloadDecoder.decodeJson).getOrElse(???)

  "The Converter" >> {
    "Converts the payload" >> {
      converter(hook).getKind must_== "push"
    }
  }

}
