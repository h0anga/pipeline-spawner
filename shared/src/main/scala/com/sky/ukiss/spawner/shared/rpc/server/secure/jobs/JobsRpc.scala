package com.sky.ukiss.spawner.shared.rpc.server.secure.jobs

import com.sky.ukiss.spawner.shared.model.jobs.{BuildJob, JobMessage}
import io.udash.rpc.{DefaultServerUdashRPCFramework, RPC}

import scala.concurrent.Future

@RPC
trait JobsRpc {
  def sendJobMessage(jobMessage: JobMessage): Future[Unit]
  def allJobs(): Future[Seq[BuildJob]]
}

object JobsRpc extends DefaultServerUdashRPCFramework.RPCCompanion[JobsRpc]