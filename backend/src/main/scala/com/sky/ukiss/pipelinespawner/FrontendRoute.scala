package com.sky.ukiss.pipelinespawner

import org.apache.commons.lang3.StringUtils
import org.scalatra.{NotFound, ScalatraServlet}

import scala.io.Source

class FrontendRoute extends ScalatraServlet {
  get("/") {
    redirect("/static/content/index.html")
  }

  get("/static/*") {
    val resourcePath = getResourcePath
    Option(servletContext.getResourceAsStream(resourcePath)) match {
      case Some(inputStream) => Source.fromInputStream(inputStream).toArray
      case None          => NotFound
    }
  }

  private def getResourcePath =
    if (StringUtils.isEmpty(splatPath)) {
      request.getServletPath
    } else {
      request.getServletPath + "/" + splatPath
    }

  private def splatPath = multiParams("splat").head
}
