package com.sky.ukiss.spawner.backend.rpc.secure

import com.sky.ukiss.spawner.backend.rpc.secure.chat.ChatEndpoint
import com.sky.ukiss.spawner.backend.services.DomainServices
import com.sky.ukiss.spawner.shared.model.auth.UserContext
import com.sky.ukiss.spawner.shared.rpc.server.secure.SecureRPC
import com.sky.ukiss.spawner.shared.rpc.server.secure.chat.ChatRPC

class SecureEndpoint(implicit domainServices: DomainServices, ctx: UserContext) extends SecureRPC {
  import domainServices._

  lazy val chatEndpoint = new ChatEndpoint

  override def chat(): ChatRPC = chatEndpoint
}
