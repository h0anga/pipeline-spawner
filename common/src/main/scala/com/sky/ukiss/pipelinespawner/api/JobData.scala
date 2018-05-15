package com.sky.ukiss.pipelinespawner.api

case class JobData(id: JobId, appName: String, status: JobStatus, podLogs: String)
