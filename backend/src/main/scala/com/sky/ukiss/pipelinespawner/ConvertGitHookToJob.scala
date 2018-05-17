package com.sky.ukiss.pipelinespawner

import java.time.Clock

import com.sky.ukiss.pipelinespawner.hooks.GithubPayload
import com.sky.ukiss.pipelinespawner.utils.Utils._
import io.fabric8.kubernetes.api.model._

import scala.collection.JavaConverters._

class ConvertGitHookToJob(generateId: () => String,
                          clock: Clock,
                          artifactoryUserName: String,
                          artifactoryPassword: String) extends (GithubPayload => Job) {

  private val repo = "repo.sns.sky.com:8186"
  private val version = "1.0.13"
  private val buildImage = s"$repo/dost/pipeline-build:$version"
  private val myName = "pipeline-spawner"

  override def apply(hook: GithubPayload): Job = {
    val metadata = new ObjectMeta()
    val id = generateId()
    metadata.setName(s"$myName-$id")
    metadata.setLabels(Map("app_name" -> myName, "app_building" -> hook.repository.name).asJava)

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

    container.setEnv(List(userNameEnvVar, passwordEnvVar, goPipelineLabel).asJava)
    container.setImage(buildImage)
    container.setName("build")

    val volumeMount = new VolumeMount()
    volumeMount.setName("private-key")
    volumeMount.setMountPath("/build/.ssh")
    container.setVolumeMounts(List(volumeMount).asJava)

    val volume = new Volume()
    volume.setName("private-key")
    val secretVolumeSource = new SecretVolumeSource()
    secretVolumeSource.setSecretName("spawner-key-secret")
    volume.setSecret(secretVolumeSource)

    podSpec.setVolumes(List(volume).asJava)

    val cloneUrl = hook.project.map(_.git_http_url).getOrElse(hook.repository.url)
    val commit = hook.after
    container.setCommand(List(
      "bash", "-c",
      s"git clone $cloneUrl application && cd application/pipeline && git checkout $commit && make build push"
    ).asJava)

    job
  }

  private val userNameEnvVar = new EnvVar(
    "ARTIFACTORY_USERNAME",
    artifactoryUserName,
    null
  )

  private val passwordEnvVar = new EnvVar(
    "ARTIFACTORY_PASSWORD",
    artifactoryPassword,
    null
  )

  private def goPipelineLabel = {
    new EnvVar(
      "GO_PIPELINE_LABEL",
      formattedTimestamp(clock.instant()),
      null
    )
  }
}

