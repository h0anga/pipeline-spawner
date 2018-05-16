package com.sky.ukiss.pipelinespawner

import com.typesafe.config._

object SpawnerConfig {
  val env = sys.env.getOrElse("ENVIRONMENT", "development")

  val conf = ConfigFactory.load()
  def apply(): Config = conf.getConfig(env)
}