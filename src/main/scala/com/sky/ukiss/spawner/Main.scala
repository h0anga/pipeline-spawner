package com.sky.ukiss.spawner

import org.mortbay.jetty.Server
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.webapp.WebAppContext

object Main extends App {
  val server = new Server
  val scc = new SelectChannelConnector
  scc.setPort(8080)
  server.setConnectors(Array(scc))

  val context = new WebAppContext()
  context.setServer(server)
  context.setContextPath("/")
  context.setWar("src/main/webapp")

  server.addHandler(context)

  server.start()
  server.join()
}
