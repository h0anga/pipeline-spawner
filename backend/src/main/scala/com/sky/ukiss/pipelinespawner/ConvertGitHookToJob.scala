package com.sky.ukiss.pipelinespawner

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime, ZoneId}

import com.sky.ukiss.pipelinespawner.hooks.GitHookPayload
import com.sky.ukiss.pipelinespawner.utils.Utils
import com.sky.ukiss.pipelinespawner.utils.Utils._
import io.fabric8.kubernetes.api.model._

import scala.collection.JavaConverters._

class ConvertGitHookToJob(generateId: () => String) extends (GitHookPayload => Job) {

  private val repo = "repo.sns.sky.com:8186"
  private val version = "0.1.5"
  private val buildImage = s"$repo/dost/pipeline-build:$version"
  private val myName = "pipeline-spawner"



  override def apply(hook: GitHookPayload): Job = {
    val metadata = new ObjectMeta()
    val id = generateId()
    metadata.setName(s"$myName-$id")
    metadata.setLabels(Map("app_name" -> myName).asJava)

    val job = new Job()
    val spec = new JobSpec()
    val podTemplateSpec = new PodTemplateSpec()
    val podSpec = new PodSpec()
    val container = new Container()

    job.setSpec(spec)
    job.setMetadata(metadata)

    spec.setTemplate(podTemplateSpec)

    podTemplateSpec.setMetadata(metadata)
    podTemplateSpec.setSpec(podSpec)

    podSpec.setRestartPolicy("Never")
    podSpec.setContainers(List(container).asJava)

    container.setEnv(List(artifactoryUserName, artifactoryPassword, goPipelineLabel).asJava)
    container.setImage(buildImage)
    container.setName("build")

    val cloneUrl = hook.project.git_http_url
    val commit = hook.after
    container.setCommand(List(
      "bash", "-c",
      s"git clone $cloneUrl application && cd application/pipeline && git checkout $commit && make build push"
    ).asJava)

    job
  }

  private val artifactoryUserName = new EnvVar(
    "ARTIFACTORY_USERNAME",
    Config().getString("pipeline-spawner.artifactoryUsername"),
    null)

  private val artifactoryPassword = new EnvVar(
    "ARTIFACTORY_PASSWORD",
    Config().getString("pipeline-spawner.artifactoryPassword"),
    null)

  private def goPipelineLabel = {
    val goPipelineLabel = new EnvVar()
    goPipelineLabel.setName("GO_PIPELINE_LABEL")
    goPipelineLabel.setValue(formattedTimestamp(Instant.now()))
    goPipelineLabel
  }
}

