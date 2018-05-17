package com.sky.ukiss.pipelinespawner

import java.time.Clock

import com.sky.ukiss.pipelinespawner.hooks.GithubPayload
import com.sky.ukiss.pipelinespawner.utils.Utils._
import io.fabric8.kubernetes.api.model._

import scala.collection.JavaConverters._

class ConvertGitHookToJob(generateId: () => String,
                          clock: Clock) extends (GithubPayload => Job) {

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


    container.setVolumeMounts(List(
      volumeMount("private-key", "/build/.ssh")
    ).asJava)

    podSpec.setVolumes(List(
      volume("private-key", "spawner-key-secret")
    ).asJava)

    val cloneUrl = hook.project.map(_.git_http_url).getOrElse(hook.repository.url)
    val commit = hook.after
    container.setCommand(List(
      "bash", "-c",
      s"git clone $cloneUrl application && cd application/pipeline && git checkout $commit && make build push"
    ).asJava)

    job
  }

  private def volumeMount(name:String, mountPath: String) = {
    val volumeMount = new VolumeMount()
    volumeMount.setName(name)
    volumeMount.setMountPath(mountPath)
    volumeMount
  }

  private def volume(name:String, secretName: String) = {
    val volume = new Volume()
    volume.setName(name)
    val secretVolumeSource = new SecretVolumeSource()
    secretVolumeSource.setSecretName(secretName)
    volume.setSecret(secretVolumeSource)
    volume
  }

  private def envVarSource(key: String, name: String) = {
    val envVarSource = new EnvVarSource()
    envVarSource.setSecretKeyRef(new SecretKeySelector(key, name, false))
    envVarSource
  }


  private val userNameEnvVar = new EnvVar(
    "ARTIFACTORY_USERNAME",
    null,
    envVarSource("spawner-artifactory-secret", "artifactory.user")
  )

  private val passwordEnvVar = new EnvVar(
    "ARTIFACTORY_PASSWORD",
    null,
    envVarSource("spawner-artifactory-secret", "artifactory.password")
  )

  private def goPipelineLabel = {
    new EnvVar(
      "GO_PIPELINE_LABEL",
      formattedTimestamp(clock.instant()),
      null
    )
  }
}

