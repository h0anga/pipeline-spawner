package com.sky.ukiss.pipelinespawner.routes

import com.sky.ukiss.pipelinespawner.status.AppStatus
import org.scalatra.{Ok, ScalatraServlet}


class StatusRoute(status: AppStatus) extends ScalatraServlet {
  get("/") {
    Ok(status().toJson)
  }
}
