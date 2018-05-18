package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api.{Active, JobCreated, JobData, JobEvent}
import org.specs2.mutable.Specification

class ApiTest extends Specification {
  args(sequential = true)

  "The JobEvent should" >> {
    val jobEvent = JobCreated("123", JobData("123", "foobar", Active, "1970-01-01-12.00.00"))

    var serialized: String = ""

    "be serializable to string" >> {
      serialized = jobEvent.asJson
      println("in serialisation test: " + serialized)
      serialized must contain("foobar") and contain("123") and contain("Active")
    }

    "be deserializable from string" >> {
      println("in deserialisation test: " + serialized)
      val parsedEvent = JobEvent.fromString(serialized)

      (parsedEvent.isSuccess must_== true) and (
        parsedEvent.get must haveClass[JobCreated]
      ) and (
        parsedEvent.get.asInstanceOf[JobCreated].job.id must_== "123"
        )and (
        parsedEvent.get.asInstanceOf[JobCreated].job.appName must_== "foobar"
        )and (
        parsedEvent.get.asInstanceOf[JobCreated].job.status must_== Active
        )
    }
  }

}
