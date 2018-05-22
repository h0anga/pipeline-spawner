package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api.{Active, Failed, JobChanged, JobCreated, JobData, JobDeleted, JobId, Succeeded, Unknown}
import io.fabric8.kubernetes.api.model.{Job, Pod}
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
      j.getMetadata.getLabels.get("app_building"),
      jobStatus(j),
      j.getStatus.getStartTime
    )
  }

  private def jobStatus(j: Job): api.JobStatus = {
    val status = j.getStatus
    val potentialPod = client.pods().inNamespace(namespace).withLabel("job-name", j.getMetadata.getName).list().getItems
    if (potentialPod.isEmpty) return Active

    val pod = potentialPod.get(0)

    println(pod)

    val completed = pod.getStatus.getContainerStatuses.asScala
      .filter(_.getName == "build")
      .map(_.getState.getTerminated)
      .filter(_ != null)
      .find(_.getReason == "Completed")

    if (completed.isDefined) Succeeded
    else if (status.getActive == 1) Active
    else if (status.getFailed == 1) Failed
    else if (status.getSucceeded == 1) Succeeded
    else Unknown
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
        case ADDED | MODIFIED =>
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

  client.pods().inNamespace(namespace).watch(new Watcher[Pod] {
    override def eventReceived(action: Watcher.Action, resource: Pod): Unit = {
      if (resource.getMetadata.getLabels.get("app_name") == "pipeline-spawner") {
        if (theBuildContainerIsTerminated(resource)) {
          resource.getStatus.getContainerStatuses.asScala.foreach(status => {
            val job = client.extensions().jobs().inNamespace(namespace).withName(resource.getMetadata.getLabels.get("job-name"))
            val jobData = convertToJobData(job.get())
            broadcaster.broadcast(JobChanged(jobData.id, jobData))
          })
        }
      }
    }

    override def onClose(cause: KubernetesClientException): Unit = ???
  })


  private def theBuildContainerIsTerminated(resource: Pod): Boolean = resource.getStatus.getContainerStatuses.asScala
    .filter(_.getName == "build")
    .filter(_.getState.getTerminated != null)
    .toList.nonEmpty

}