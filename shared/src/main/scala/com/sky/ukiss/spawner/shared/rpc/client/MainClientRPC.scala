package com.sky.ukiss.spawner.shared.rpc.client

import com.sky.ukiss.spawner.shared.rpc.client.chat.ChatNotificationsRPC
import io.udash.rpc._

@RPC
trait MainClientRPC {
  def chat(): ChatNotificationsRPC
}

object MainClientRPC extends DefaultClientUdashRPCFramework.RPCCompanion[MainClientRPC]