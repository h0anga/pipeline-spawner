package com.sky.ukiss.pipelinespawner

//object Server extends StreamApp[IO] with Http4sDsl[IO] {
//
//  def stream(args: List[String], requestShutdown: IO[Unit]) = {
//    val context = new Context
//    BlazeBuilder[IO]
//      .bindHttp(8080, "0.0.0.0")
//      .mountService(context.gitHookServiceComponent.service, "/")
//      .mountService(context.frontendRoute.service, "/")
//      .serve
//  }
//
//}
//