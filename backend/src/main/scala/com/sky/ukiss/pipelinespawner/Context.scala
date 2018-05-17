package com.sky.ukiss.pipelinespawner

import java.time.Clock

import com.sky.ukiss.pipelinespawner.routes.{FrontendRoute, GitHookServiceComponent, LogRoute, WebSocketComponent}
import com.typesafe.config.Config
import io.fabric8.kubernetes.client.DefaultKubernetesClient

import scala.util.Random

class Context(config: Config) {
  lazy val namespace: String = config.getString("pipeline-spawner.namespace")
  lazy val kubernetesService = new KubernetesService(kubernetesClient, namespace, gitHookPayloadToJobConverter)
  lazy val kubernetesClient = new DefaultKubernetesClient()
  lazy val logProvider = new LogProvider(kubernetesClient, namespace)
  lazy val logRoute = new LogRoute(logProvider)
  lazy val generateRandomId: () => String = () => Random.alphanumeric.filter(c => c.isDigit || c.isLower).take(6).mkString
  lazy val gitHookPayloadToJobConverter = new ConvertGitHookToJob(
    generateRandomId,
    Clock.systemUTC()
  )
  lazy val gitHookServiceComponent = new GitHookServiceComponent(kubernetesService)
  lazy val atmosphereJobEventBroadcaster = new AtmosphereJobEventBroadcaster
  lazy val jobEvents = new JobEvents(kubernetesClient, namespace, atmosphereJobEventBroadcaster)
  lazy val webSocketComponent = new WebSocketComponent(jobEvents)
  lazy val frontendRoute = new FrontendRoute()

}
