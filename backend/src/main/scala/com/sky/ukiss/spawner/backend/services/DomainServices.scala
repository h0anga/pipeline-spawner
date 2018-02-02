package com.sky.ukiss.spawner.backend.services

/** Container for all services used to implicitly pass services to endpoints. */
class DomainServices(implicit
  val authService: AuthService,
  val chatService: ChatService,
  val rpcClientsService: RpcClientsService
)
