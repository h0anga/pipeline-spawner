package com.sky.ukiss.spawner.shared.rpc.server.secure

import com.sky.ukiss.spawner.shared.rpc.server.secure.chat.ChatRPC
import com.sky.ukiss.spawner.shared.rpc.server.secure.jobs.JobsRpc
import io.udash.rpc._

@RPC
trait SecureRPC {
  def chat(): ChatRPC
  def jobs(): JobsRpc
}

object SecureRPC extends DefaultServerUdashRPCFramework.RPCCompanion[SecureRPC]
