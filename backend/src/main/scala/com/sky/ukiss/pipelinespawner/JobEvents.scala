package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api.{JobChanged, JobCreated, JobData, JobDeleted, JobId}
import io.fabric8.kubernetes.api.model.Job
import io.fabric8.kubernetes.client.Watcher.Action._
import io.fabric8.kubernetes.client.{KubernetesClient, KubernetesClientException, Watcher}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

class JobEvents(client: KubernetesClient,
                namespace: String,
                broadcaster: JobEventBroadcaster
               ) {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val currentJobs: mutable.Map[JobId, JobData] = mutable.Map()


  def getCurrentJobs: mutable.Map[JobId, JobData] = currentJobs

  private def convertToJobData(j: Job) = JobData(j.getMetadata.getName, j.getMetadata.getLabels.get("app_name"))

  logger.info("Getting initial jobs")

  currentJobs ++= client.extensions().jobs()
    .inNamespace(namespace)
    .list().getItems.asScala
    .map(convertToJobData)
    .map(data => (data.id, data))
    .toMap

  logger.info("Registering watcher with Kubernetes")

  client.extensions().jobs().inNamespace(namespace).watch(new Watcher[Job] {
    override def onClose(cause: KubernetesClientException): Unit = ???

    override def eventReceived(action: Watcher.Action, job: Job): Unit = {
      val jobData = convertToJobData(job)

      action match {
        case ADDED|MODIFIED =>
          currentJobs(jobData.id) = jobData
          val jobId: JobId = jobData.id
          if (action == ADDED)
            broadcaster.broadcast(JobCreated(jobId, jobData))
          else
            broadcaster.broadcast(JobChanged(jobId, jobData))
        case DELETED =>
          currentJobs.remove(jobData.id)
          broadcaster.broadcast(JobDeleted(jobData.id))
        case ERROR => logger.error("error about job events", job)
      }

    }
  })
}