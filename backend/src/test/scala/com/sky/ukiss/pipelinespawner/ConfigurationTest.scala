package com.sky.ukiss.pipelinespawner

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class ConfigurationTest extends Specification with Mockito {

  "Configuration" >> {
    val userName = SpawnerConfig().getString("pipeline-spawner.artifactoryUsername")

    "return mango" >> {
      userName must_== "mango"
    }

  }
}
