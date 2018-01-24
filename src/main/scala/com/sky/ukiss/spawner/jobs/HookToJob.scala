package com.sky.ukiss.spawner.jobs

import java.util.UUID

import io.fabric8.kubernetes.api.model._
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.handlers.JobHandler

import scala.collection.JavaConverters._

class HookToJob(kubernetes: KubernetesClient) {

  private val buildImageVersion = "0.1.8"
  private val buildImage = s"repo.sns.sky.com:8186/dost/pipeline-build:$buildImageVersion"

  private def container(hook: HookData) = {
    val buildContainer = new Container()
    buildContainer.setImage(buildImage)
    buildContainer.setCommand(List(
      "bash", "-c",
      s"git clone ${hook.repository.homepage}.git application && cd application && make build"
    ).asJava)
    buildContainer.setName("pipeline-spawner-job")
    buildContainer.setEnv(List(
      new EnvVar("GIT_SSL_NO_VERIFY", "1", null)
    ).asJava)
    buildContainer
  }

  private def podSpec(hook: HookData) = {
    val spec = new PodSpec()
    spec.setContainers(List(container(hook)).asJava)
    spec.setRestartPolicy("Never")
    spec
  }

  private def objectMeta(hook: HookData)(implicit id : String) = {
    val meta = new ObjectMeta()
    meta.setLabels(Map(
      "app_name" -> "pipeline-spawner",
      "app_version" -> "0.0.1-dev",
      "environment" -> "minikube",
      "location" -> "local",
      "dev_team_responsible" -> "alberto.colombo"
    ).asJava)
    meta.setName(s"pipeline-spawner-job-$id")
    meta
  }

  private def jobSpec(hook: HookData)(implicit id : String) = {
    val spec = new JobSpec()
    spec.setTemplate(new PodTemplateSpec(objectMeta(hook), podSpec(hook)))
    spec.setParallelism(1)
    spec.setCompletions(1)
    spec.setSelector(new LabelSelector())
    spec
  }

  def submit(hook: HookData): Unit = {
    implicit val id = UUID.randomUUID().toString
    kubernetes
      .resource(
        new Job(
          "batch/v1",
          "Job",
          objectMeta(hook),
          jobSpec(hook),
          null
        )
      )
      .inNamespace("mobile")
      .createOrReplace()
  }
}
