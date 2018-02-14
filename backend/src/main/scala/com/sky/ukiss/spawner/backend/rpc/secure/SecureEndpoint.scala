package com.sky.ukiss.spawner.backend.rpc.secure

import com.sky.ukiss.spawner.backend.rpc.secure.chat.ChatEndpoint
import com.sky.ukiss.spawner.backend.rpc.secure.jobs.JobsEndpoint
import com.sky.ukiss.spawner.backend.services.DomainServices
import com.sky.ukiss.spawner.shared.model.auth.UserContext
import com.sky.ukiss.spawner.shared.rpc.server.secure.SecureRPC
import com.sky.ukiss.spawner.shared.rpc.server.secure.chat.ChatRPC
import com.sky.ukiss.spawner.shared.rpc.server.secure.jobs.JobsRpc

class SecureEndpoint(implicit domainServices: DomainServices, ctx: UserContext) extends SecureRPC {
  import domainServices._

  lazy val chatEndpoint = new ChatEndpoint
  lazy val jobsEndpoint = new JobsEndpoint

  override def chat(): ChatRPC = chatEndpoint

  override def jobs(): JobsRpc = jobsEndpoint
}
