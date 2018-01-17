package com.sky.ukiss.spawner.jobs

import io.fabric8.kubernetes.api.model._
import io.fabric8.kubernetes.client.KubernetesClient

import scala.collection.JavaConverters._

class HookToJob(kubernetes: KubernetesClient) {

  private val buildImageVersion = "0.1.8"
  private val buildImage = s"repo.sns.sky.com:8186/dost/pipeline-build:$buildImageVersion"

  private val container = {
    val buildContainer = new Container()
    buildContainer.setImage(buildImage)
    buildContainer.setCommand(List(
      "bash", "-c",
      "git clone https://git.sns.sky.com/alberto.colombo/aggro-cli.git application && cd application && make"
    ).asJava)
    buildContainer.setName("pipeline-spawner-job")
    buildContainer.setEnv(List(
      new EnvVar("GIT_SSL_NO_VERIFY", "1", null)
    ).asJava)
    buildContainer
  }

  private val podSpec = {
    val spec = new PodSpec()
    spec.setContainers(List(container).asJava)
    spec.setRestartPolicy("Never")
    spec
  }

  private val objectMeta = {
    val meta = new ObjectMeta()
    meta.setLabels(Map(
      "app_name" -> "pipeline-spawner",
      "app_version" -> "0.0.1-dev",
      "environment" -> "minikube",
      "location" -> "local",
      "dev_team_responsible" -> "alberto.colombo"
    ).asJava)
    meta
  }

  private val jobSpec = {
    val spec = new JobSpec()
    spec.setTemplate(new PodTemplateSpec(objectMeta, podSpec))
    spec.setParallelism(1)
    spec.setCompletions(1)
    spec
  }

  def submit(hook: HookData): Unit = {
    kubernetes.resource(
      new Job(
        "batch/v1",
        "Job",
        objectMeta,
        jobSpec,
        null
      )
    ).createOrReplace()
  }
}
