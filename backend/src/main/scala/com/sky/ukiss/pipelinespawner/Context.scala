package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.routes.{FrontendRoute, GitHookServiceComponent, WebSocketComponent}
import io.fabric8.kubernetes.client.DefaultKubernetesClient

import scala.util.Random

class Context {
  lazy val namespace: String = Config().getString("pipeline-spawner.namespace")
  lazy val kubernetesService = new KubernetesService(kubernetesClient, namespace, gitHookPayloadToJobConverter)
  lazy val kubernetesClient = new DefaultKubernetesClient()
  lazy val generateRandomId: () => String = () => Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(6).mkString
  lazy val gitHookPayloadToJobConverter = new ConvertGitHookToJob(generateRandomId)
  lazy val gitHookServiceComponent = new GitHookServiceComponent(kubernetesService)
  lazy val atmosphereJobEventBroadcaster = new AtmosphereJobEventBroadcaster
  lazy val jobEvents = new JobEvents(kubernetesClient, namespace, atmosphereJobEventBroadcaster)
  lazy val webSocketComponent = new WebSocketComponent(jobEvents)
  lazy val frontendRoute = new FrontendRoute()

}
