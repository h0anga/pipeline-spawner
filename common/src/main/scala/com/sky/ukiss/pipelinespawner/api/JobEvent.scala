package com.sky.ukiss.pipelinespawner.api;

trait JobEvent

case class JobCreated(job: Job) extends JobEvent
