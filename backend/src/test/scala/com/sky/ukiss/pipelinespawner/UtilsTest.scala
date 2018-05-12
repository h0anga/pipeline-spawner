package com.sky.ukiss.pipelinespawner

import java.time.Instant

import com.sky.ukiss.pipelinespawner.utils.Utils._
import org.specs2.mutable.Specification

class UtilsTest extends Specification {
  "Utils" >> {
    "should provide a unix timestamp formatted with seconds precision as yyyyMMddHHmmss" >> {

      val actualTimestamp = formattedTimestamp(Instant.parse("2018-05-11T15:00:12Z"))
      val expectedTimastamp = "20180511150012"
      actualTimestamp mustEqual expectedTimastamp
    }
  }
}

