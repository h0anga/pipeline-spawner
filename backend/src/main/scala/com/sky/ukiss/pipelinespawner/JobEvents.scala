package com.sky.ukiss.pipelinespawner

import cats.effect.IO
import com.sky.ukiss.pipelinespawner.api.{JobCreated, JobEvent, Job => JobData}
import fs2.async.mutable.Topic
import io.fabric8.kubernetes.api.model.Job
import io.fabric8.kubernetes.client.Watcher.Action._
import io.fabric8.kubernetes.client.{KubernetesClient, KubernetesClientException, Watcher}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable

class JobEvents(client: KubernetesClient,
                namespace: String,
                jobEventsTopic: Topic[IO, JobEvent]
               ) {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val jobsCell: mutable.Set[JobData] = mutable.Set()

  private def jobs = jobsCell.seq

  logger.info("Getting initial jobs")

  jobs ++= client.extensions().jobs()
    .inNamespace(namespace)
    .list().getItems.asScala
    .map((j: Job) => JobData(j.getMetadata.getName, j.getMetadata.getLabels.get("app_name")))
    .toSet

  logger.info("Registering watcher with Kubernetes")

  client.extensions().jobs().inNamespace(namespace).watch(new Watcher[Job] {
    override def onClose(cause: KubernetesClientException): Unit = ???

    override def eventReceived(action: Watcher.Action, job: Job): Unit = {
      val name = job.getMetadata.getName
      logger.info("Job changed: " + name)
      val labels = job.getMetadata.getLabels
      val jobData = JobData(name, labels.get("app_name"))

      action match {
        case ADDED | MODIFIED => jobs += jobData
        case DELETED => jobs -= jobData
        case ERROR => logger.error("error about job events", job)
      }

      jobEventsTopic.publish1(JobCreated(jobData)).unsafeRunSync()
    }
  })

}