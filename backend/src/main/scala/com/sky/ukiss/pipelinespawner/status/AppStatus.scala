package com.sky.ukiss.pipelinespawner.status

import com.sky.ukiss.pipelinespawner.JobEvents

class AppStatus(appVersion: String,
                jobEvents: JobEvents,
                baseUrl: String
               ) {
  def apply() = StatusPage(
    applicationVersion = appVersion,
    probes = List(
      Probe("JobSize", "Number of running jobs: " + jobEvents.getCurrentJobs.size, ProbeStatus.OK)
    ),
    optionalValues = Map("Server Address" -> baseUrl)
  )
}
