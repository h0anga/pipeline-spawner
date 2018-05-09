package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.routes.{FrontendRoute, GitHookServiceComponent, WebSocketComponent}
import io.fabric8.kubernetes.client.DefaultKubernetesClient

import scala.util.Random

class Context {
  implicit lazy val executionContext = scala.concurrent.ExecutionContext.Implicits.global
  lazy val kubernetesService = new KubernetesService(kubernetesClient, namespace, gitHookPayloadToJobConverter)
  lazy val kubernetesClient = new DefaultKubernetesClient()
  lazy val namespace = "mobile" // TODO read from config file?
  lazy val gitHookPayloadToJobConverter = new ConvertGitHookToJob(generateRandomId)
  lazy val generateRandomId = () => Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(6).mkString
  lazy val gitHookServiceComponent = new GitHookServiceComponent(kubernetesService)
  lazy val jobEvents = new JobEvents(kubernetesClient, namespace)
  lazy val webSocketComponent = new WebSocketComponent(jobEvents)
  lazy val artifactoryUsername = Config().getString("pipeline-spawner.artifactoryUsername")
  lazy val artifactoryPassword = Config().getString("pipeline-spawner.artifactoryPassword")
  lazy val frontendRoute = new FrontendRoute()

}
