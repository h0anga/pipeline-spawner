package com.sky.ukiss.pipelinespawner.routes

import com.sky.ukiss.pipelinespawner.LogProvider
import org.scalatra.{Ok, ScalatraServlet}

class LogRoute(logProvider: LogProvider) extends ScalatraServlet {
  get("/:jobId") {
    Ok(logProvider.podLogs(params("jobId")))
  }
}
