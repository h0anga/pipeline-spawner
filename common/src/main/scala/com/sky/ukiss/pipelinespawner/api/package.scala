package com.sky.ukiss.pipelinespawner

import prickle.{CompositePickler, PicklerPair}

package object api {
  type JobId = String

  sealed trait JobEvent

  case object NoJobEvent extends JobEvent

  case class JobCreated(job: Job) extends JobEvent

  case class JobDeleted(id: JobId) extends JobEvent

  implicit val messagePickler: PicklerPair[JobEvent] = CompositePickler[JobEvent]
    .concreteType[NoJobEvent.type]
    .concreteType[JobCreated]
    .concreteType[JobDeleted]
}
