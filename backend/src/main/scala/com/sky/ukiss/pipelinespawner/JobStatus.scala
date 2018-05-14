package com.sky.ukiss.pipelinespawner

object JobStatus extends Enumeration {
  type JobStatus = Value
  val Active, Failed, Succeeded = Value
}
