package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api.{Job, JobCreated, JobEvent}
import org.specs2.mutable.Specification

class ApiTest extends Specification {
  args(sequential = true)

  "The JobEvent should" >> {
    val jobEvent = JobCreated(Job("123", "foobar"))

    var serialized: String = ""

    "be serializable to string" >> {
      serialized = jobEvent.asJson
      println("in serialisation test: " + serialized)
      serialized must contain("foobar") and contain("123")
    }

    "be deserializable from string" >> {
      println("in deserialisation test: " + serialized)
      val parsedEvent = JobEvent.fromString(serialized)

      (parsedEvent.isSuccess must_== true) and (
        parsedEvent.get must haveClass[JobCreated]
      ) and (
        parsedEvent.get.asInstanceOf[JobCreated].job.id must_== "123"
        )
    }
  }

}
