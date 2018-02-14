package com.sky.ukiss.spawner.shared.model.jobs

import com.avsystem.commons.serialization.HasGenCodec

case class BuildJob(name: String, id: String)
object BuildJob extends HasGenCodec[BuildJob]

abstract sealed class JobMessage
object JobMessage extends HasGenCodec[JobMessage]

case class JobCreated(job: BuildJob) extends JobMessage
object JobCreated extends HasGenCodec[JobCreated]

case class JobDeleted(job: BuildJob) extends JobMessage
object JobDeleted extends HasGenCodec[JobDeleted]

case class JobChanged(job: BuildJob) extends JobMessage
object JobChanged extends HasGenCodec[JobChanged]

