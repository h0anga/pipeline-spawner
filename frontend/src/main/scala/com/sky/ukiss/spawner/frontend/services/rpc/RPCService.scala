package com.sky.ukiss.spawner.frontend.services.rpc

import com.sky.ukiss.spawner.shared.rpc.client.MainClientRPC
import com.sky.ukiss.spawner.shared.rpc.client.chat.ChatNotificationsRPC

class RPCService(notificationsCenter: NotificationsCenter) extends MainClientRPC {
  override val chat: ChatNotificationsRPC =
    new ChatService(notificationsCenter.msgListeners, notificationsCenter.connectionsListeners)
}