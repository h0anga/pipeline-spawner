package com.sky.ukiss.pipelinespawner

import io.fabric8.kubernetes.client.DefaultKubernetesClient

import scala.util.Random

class Configuration {
  lazy val kubernetesService = new KubernetesService(kubernetesClient, namespace, gitHookPayloadToJobConverter)
  lazy val kubernetesClient = new DefaultKubernetesClient()
  lazy val namespace = "mobile" // TODO read from config file?
  lazy val gitHookPayloadToJobConverter = new ConvertGitHookToJob(generateRandomId)
  lazy val generateRandomId = () => Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(6).mkString
  lazy val gitHookServiceComponent = new GitHookServiceComponent(kubernetesService)
  lazy val artifactoryUsername = Config().getString("pipeline-spawner.artifactoryUsername")
  lazy val artifactoryPassword = Config().getString("pipeline-spawner.artifactoryPassword")

}
