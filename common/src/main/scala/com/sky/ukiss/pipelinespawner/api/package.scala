package com.sky.ukiss.pipelinespawner

package object api {
  type JobId = String

  case class WsMessage(payload: Payload)

  sealed trait Payload
  case object Ping extends Payload
  case object Pong extends Payload

  sealed trait JobEvent extends Payload
  case object NoJobEvent extends JobEvent
  case class JobCreated(job: Job) extends JobEvent
  case class JobDeleted(id: JobId) extends JobEvent
}
