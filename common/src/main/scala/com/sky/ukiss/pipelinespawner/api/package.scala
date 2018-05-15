package com.sky.ukiss.pipelinespawner

import prickle.{CompositePickler, Pickle, PicklerPair, Unpickle}

import scala.util.Try

package object api {
  type JobId = String

  sealed trait JobEvent {
    final def asJson: String = Pickle.intoString(this)
  }

  object JobEvent {
    def fromString(s: String): Try[JobEvent] = Unpickle[JobEvent].fromString(s)
  }

  case object NoJobEvent$ extends JobEvent

  case class JobCreated(id: JobId, job: JobData) extends JobEvent

  case class JobChanged(id: JobId, job: JobData) extends JobEvent

  case class JobDeleted(id: JobId) extends JobEvent

  private implicit val messagePickler: PicklerPair[JobEvent] = CompositePickler[JobEvent]
    .concreteType[NoJobEvent$.type]
    .concreteType[JobCreated]
    .concreteType[JobChanged]
    .concreteType[JobDeleted]

  sealed trait JobStatus

  case object Active extends JobStatus

  case object Succeeded extends JobStatus

  case object Failed extends JobStatus

  case object Unknown extends JobStatus

  private implicit val jobStatusPickler: PicklerPair[JobStatus] = CompositePickler[JobStatus]
    .concreteType[Succeeded.type]
    .concreteType[Active.type]
    .concreteType[Failed.type]
    .concreteType[Unknown.type]

}
