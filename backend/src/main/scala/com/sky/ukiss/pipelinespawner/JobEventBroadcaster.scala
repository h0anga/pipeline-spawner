package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api.JobEvent

trait JobEventBroadcaster {
  def broadcast(event: JobEvent): Unit
}
