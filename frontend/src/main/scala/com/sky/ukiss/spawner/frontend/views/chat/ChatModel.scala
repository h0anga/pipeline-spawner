package com.sky.ukiss.spawner.frontend.views.chat

import com.sky.ukiss.spawner.shared.model.chat.ChatMessage
import io.udash._

case class ChatModel(msgs: Seq[ChatMessage], msgInput: String, connectionsCount: Int)
object ChatModel extends HasModelPropertyCreator[ChatModel]
