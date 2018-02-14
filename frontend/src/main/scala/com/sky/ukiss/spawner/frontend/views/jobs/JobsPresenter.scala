package com.sky.ukiss.spawner.frontend.views.jobs

import com.sky.ukiss.spawner.frontend.routing.ViewAllJobsState
import com.sky.ukiss.spawner.frontend.services.rpc.NotificationsCenter
import com.sky.ukiss.spawner.shared.model.jobs.{JobChanged, JobCreated, JobDeleted}
import com.sky.ukiss.spawner.shared.rpc.server.secure.jobs.JobsRpc
import io.udash.Presenter
import io.udash.auth.AuthRequires
import io.udash.properties.model.ModelProperty

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

class JobsPresenter(model: ModelProperty[JobsModel],
                    jobsRpc: JobsRpc,
                    // if auth is required, inject userService: UserContextService,
                    notificationsCenter: NotificationsCenter
                   )(
                     implicit ec: ExecutionContext
                   ) extends Presenter[ViewAllJobsState.type] with AuthRequires
{
  private val newJobCreated = notificationsCenter.onJobMessage {
    case JobCreated(j) => model.subSeq(_.jobs).append(j)
    case JobDeleted(j) => model.subSeq(_.jobs).remove(j)
    case JobChanged(_) => // nothing to do for now
  }

  override def handleState(state: ViewAllJobsState.type): Unit = {
    jobsRpc.allJobs().onComplete{
      case Success(jobs) =>
        model.subProp(_.jobs).set(jobs, force = true)
        model.subProp(_.error).set(None)
      case Failure(t) =>
        model.subProp(_.error).set(Some(t.getLocalizedMessage))
        model.subProp(_.jobs).set(Seq())
    }
  }

  override def onClose(): Unit = {
    newJobCreated.cancel()
  }

}
