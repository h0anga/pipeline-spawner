package com.sky.ukiss.spawner.backend.rpc.secure.jobs

import com.sky.ukiss.spawner.shared.model.jobs.{BuildJob, JobMessage}
import com.sky.ukiss.spawner.shared.rpc.server.secure.jobs.JobsRpc

import scala.concurrent.Future

import scala.concurrent.ExecutionContext.Implicits.global

class JobsEndpoint extends JobsRpc {
  override def sendJobMessage(jobMessage: JobMessage): Future[Unit] = Future{}

  override def allJobs(): Future[Seq[BuildJob]] = {
    Future{Seq()}
  }
}
