package com.sky.ukiss.spawner.frontend.views.chat

import com.sky.ukiss.spawner.frontend.routing.ChatState
import com.sky.ukiss.spawner.frontend.services.rpc.NotificationsCenter
import com.sky.ukiss.spawner.frontend.services.{TranslationsService, UserContextService}
import com.sky.ukiss.spawner.shared.model.SharedExceptions
import io.udash._

class ChatViewFactory(
  userService: UserContextService,
  translationsService: TranslationsService,
  notificationsCenter: NotificationsCenter
) extends ViewFactory[ChatState.type] {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def create(): (View, Presenter[ChatState.type]) = {
    val model = ModelProperty[ChatModel](ChatModel(Seq.empty, "", 0))

    val rpc = userService.secureRpc()
    if (rpc.isEmpty) throw SharedExceptions.UnauthorizedException()

    val presenter = new ChatPresenter(model, rpc.get.chat(), userService, notificationsCenter)
    val view = new ChatView(model, presenter, translationsService)

    (view, presenter)
  }
}
