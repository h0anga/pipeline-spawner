package com.sky.ukiss.spawner.jobs

import io.fabric8.kubernetes.api.model.{Event, Job}
import io.fabric8.kubernetes.client.{KubernetesClient, KubernetesClientException, Watcher}

import scala.collection.parallel.mutable
import scala.collection.JavaConverters._

class JobEvents(client: KubernetesClient, namespace: String) {
  private val jobs = mutable.ParSet[JobData]()

  jobs ++= client.extensions().jobs()
      .inNamespace(namespace)
      .list().getItems.asScala
      .map((j: Job) => JobData(j.getMetadata.getName, j.getMetadata.getLabels.get("app_name")))
      .toSet

  def currentJobs = jobs.seq.toSet

  client.events().inNamespace(namespace).watch(new Watcher[Event] {
    override def onClose(cause: KubernetesClientException): Unit = ???

    override def eventReceived(action: Watcher.Action, resource: Event): Unit = {
      println(s"Event `${action.name()}` received on `$resource`")
      // TODO if event is job added/removed, make the corresponding change to `jobs`
    }
  })
}
