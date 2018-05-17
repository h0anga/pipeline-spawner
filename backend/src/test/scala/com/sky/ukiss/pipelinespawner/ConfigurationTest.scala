package com.sky.ukiss.pipelinespawner

import org.specs2.mock.Mockito
import org.specs2.mutable.Specification

class ConfigurationTest extends Specification with Mockito {

  "Configuration" >> {
    val userName = SpawnerConfig().getString("namespace")

    "return mango" >> {
      userName must_== "testNamespace"
    }

  }
}
