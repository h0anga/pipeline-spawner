package com.sky.ukiss.spawner.frontend.routing

import com.sky.ukiss.spawner.frontend.ApplicationContext
import com.sky.ukiss.spawner.frontend.views.RootViewFactory
import com.sky.ukiss.spawner.frontend.views.chat.ChatViewFactory
import com.sky.ukiss.spawner.frontend.views.login.LoginPageViewFactory
import io.udash._

class StatesToViewFactoryDef extends ViewFactoryRegistry[RoutingState] {
  def matchStateToResolver(state: RoutingState): ViewFactory[_ <: RoutingState] =
    state match {
      case RootState => new RootViewFactory(
        ApplicationContext.translationsService
      )
      case LoginPageState => new LoginPageViewFactory(
        ApplicationContext.userService, ApplicationContext.application, ApplicationContext.translationsService
      )
      case ChatState => new ChatViewFactory(
        ApplicationContext.userService, ApplicationContext.translationsService, ApplicationContext.notificationsCenter
      )
    }
}