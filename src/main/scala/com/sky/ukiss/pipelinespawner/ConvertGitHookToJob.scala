package com.sky.ukiss.pipelinespawner

import java.util.Collections
import java.util.Collections.singletonList

import io.fabric8.kubernetes.api.model._
import org.log4s

import scala.collection.JavaConverters._

class ConvertGitHookToJob(generateId: () => String) extends (GitHookPayload => Job) {

  private val log = log4s.getLogger
  private val repo = "repo.sns.sky.com:8186"
  private val version = "0.1.5"
  private val buildImage = s"$repo/dost/pipeline-build:$version"
  private val myName = "pipeline-spanwer"

  override def apply(hook: GitHookPayload): Job = {
    val metadata = new ObjectMeta()
    val id = generateId()
    metadata.setName(s"$myName-$id")
    metadata.setLabels(Map("app_name" -> myName).asJava)

    val job = new Job()
    val spec = new JobSpec()
    job.setSpec(spec)
    job.setMetadata(metadata)

    val podTemplateSpec = new PodTemplateSpec()
    spec.setTemplate(podTemplateSpec)

    val podSpec = new PodSpec()
    podTemplateSpec.setSpec(podSpec)
    podTemplateSpec.setMetadata(metadata)

    val container = new Container()
    podSpec.setContainers(List(container).asJava)
    container.setImage(buildImage)
    container.setName("build")

    val cloneUrl = hook.repository.url + ".git"
    container.setCommand(List(
      "bash", "-c",
      s"git clone $cloneUrl application && cd application/pipeline && make buildImage"
    ).asJava)

    log.info(s"Converted $hook into $job")

    job
  }
}
