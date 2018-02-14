package com.sky.ukiss.spawner.frontend.views.jobs

import com.sky.ukiss.spawner.shared.model.jobs.BuildJob
import io.udash.properties.HasModelPropertyCreator

case class JobsModel(jobs: Seq[BuildJob], error: Option[String])
object JobsModel extends HasModelPropertyCreator[JobsModel]
