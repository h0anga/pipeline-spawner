package com.sky.ukiss.pipelinespawner

import io.fabric8.kubernetes.api.model.LabelSelector
import io.fabric8.kubernetes.client.dsl.LogWatch
import io.fabric8.kubernetes.client.{KubernetesClient, KubernetesClientException}

import scala.collection.JavaConverters._

class LogProvider(client: KubernetesClient, namespace: String) {

  def podLogs(jobName: String): String = {
    try {
      getPodOfJob(jobName).inContainer("build").getLog
    } catch {
      case e: KubernetesClientException => ""
    }
  }

  def streamLogs(jobName: String): LogWatch = {
    getPodOfJob(jobName).inContainer("build").tailingLines(1).watchLog()
  }

  private def getPodName(jobName: String) = {
    val labelSelector = new LabelSelector
    labelSelector.setMatchLabels(Map("job-name" -> jobName).asJava)
    client.pods.inNamespace(namespace).withLabelSelector(labelSelector).list.getItems.asScala.maxBy(_.getStatus.getStartTime).getMetadata.getName
  }

  private def getPodOfJob(jobName: String) = {
    client.pods().inNamespace(namespace).withName(getPodName(jobName))
  }
}
