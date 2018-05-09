package com.sky.ukiss.pipelinespawner.routes

import org.apache.commons.lang3.StringUtils
import org.scalatra.{NotFound, Ok, ScalatraServlet}

class FrontendRoute extends ScalatraServlet {

  get("/*") {
    val resourcePath = getResourcePath.substring("/static".length)

    Option(classOf[FrontendRoute].getResourceAsStream(resourcePath)) match {
      case Some(inputStream) => org.scalatra.util.io.copy(inputStream, response.getOutputStream)
      case None          => NotFound()
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
