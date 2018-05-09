package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.hooks.GitHookPayload
import io.fabric8.kubernetes.api.model._
import org.log4s

import scala.collection.JavaConverters._

class ConvertGitHookToJob(generateId: () => String) extends (GitHookPayload => Job) {

  private val log = log4s.getLogger
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

    container.setImage(buildImage)
    container.setName("build")

    val cloneUrl = hook.project.git_http_url
    val commit = hook.after
    container.setCommand(List(
      "bash", "-c",
      s"git clone $cloneUrl application && cd application/pipeline && git checkout $commit && make build push"
    ).asJava)

    log.info(s"Converted $hook into $job")

    job
  }
}
