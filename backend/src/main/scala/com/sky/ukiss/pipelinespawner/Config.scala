package com.sky.ukiss.pipelinespawner

import com.typesafe.config._

object Config {
  val env = sys.env.getOrElse("ENVIRONMENT", "development")

  val conf = ConfigFactory.load()
  def apply() = conf.getConfig(env)
}