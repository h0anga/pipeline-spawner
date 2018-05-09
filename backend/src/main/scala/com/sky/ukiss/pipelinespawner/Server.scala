package com.sky.ukiss.pipelinespawner

import org.eclipse.jetty.server.{Server => JettyServer}
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener


object Server { // this is my entry object as specified in sbt project definition
  def main(args: Array[String]) {
    val port = 8080

    val server = new JettyServer(port)
    val context = new WebAppContext()
    context setContextPath "/"
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)

    server.start()
    server.join()
  }
}
