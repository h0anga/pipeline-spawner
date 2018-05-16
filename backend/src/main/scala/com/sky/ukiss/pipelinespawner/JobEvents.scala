package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api.{Active, Failed, JobChanged, JobCreated, JobData, JobDeleted, JobId, Succeeded, Unknown}
import io.fabric8.kubernetes.api.model.{Job, LabelSelector}
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

  private def convertToJobData(j: Job) = {
    val jobName = j.getMetadata.getName
    JobData(
      jobName,
      j.getMetadata.getLabels.get("app_name"),
      jobStatus(j),
      podLogs(jobName)
    )
  }

  private def jobStatus(j: Job) = {
    val status = j.getStatus
    if (status.getActive == 1) Active
    else if (status.getFailed == 1) Failed
    else if (status.getSucceeded == 1) Succeeded
    else Unknown
  }

  //TODO - remove after front-end implementation
  def podLogs(jobName: String): String = {
    val labelSelector = new LabelSelector
    labelSelector.setMatchLabels(Map("job-name" -> jobName).asJava)

    val podNames = client.pods.withLabelSelector(labelSelector).list
      .getItems.asScala.map(p => p.getMetadata.getName)

    podNames
      .map(podName => s"$podName :\n ${client.pods.withName(podName).getLog}")
      .mkString("\n\n")
  }

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