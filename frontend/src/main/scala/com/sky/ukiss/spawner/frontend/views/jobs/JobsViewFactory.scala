package com.sky.ukiss.spawner.frontend.views.jobs

import com.sky.ukiss.spawner.frontend.routing.ViewAllJobsState
import com.sky.ukiss.spawner.frontend.services.UserContextService
import com.sky.ukiss.spawner.frontend.services.rpc.NotificationsCenter
import io.udash.core.{Presenter, View, ViewFactory}
import io.udash.properties.model.ModelProperty

import scala.concurrent.ExecutionContext.Implicits.global

class JobsViewFactory(
  userService: UserContextService,
  notificationsCenter: NotificationsCenter
) extends ViewFactory[ViewAllJobsState.type]{

  override def create(): (View, Presenter[ViewAllJobsState.type]) = {
    val model = ModelProperty(JobsModel(Seq(), None))
    val presenter =  new JobsPresenter(model, userService.secureRpc().map(_.jobs()).get, notificationsCenter)
    val view = new JobsView(model, presenter)

    (view, presenter)
  }
}
