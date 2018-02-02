package com.sky.ukiss.spawner.backend.rpc.auth

import com.sky.ukiss.spawner.backend.services.{AuthService, RpcClientsService}
import com.sky.ukiss.spawner.shared.model.auth.UserContext
import com.sky.ukiss.spawner.shared.rpc.server.open.AuthRPC
import io.udash.rpc.ClientId

import scala.concurrent.Future
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

class AuthEndpoint(implicit authService: AuthService, rpcClientsService: RpcClientsService, clientId: ClientId) extends AuthRPC {
  override def login(username: String, password: String): Future[UserContext] = {
    val response = authService.login(username, password)
    response.onComplete {
      case Success(ctx) =>
        // let rpcClientsService know about a new authenticated connection
        rpcClientsService.registerAuthenticatedConnection(clientId, ctx)
      case Failure(_) => // ignore
    }
    response
  }
}
