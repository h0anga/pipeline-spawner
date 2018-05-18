package com.sky.ukiss.pipelinespawner

import io.fabric8.kubernetes.api.model.LabelSelector
import io.fabric8.kubernetes.client.KubernetesClient
import scala.collection.JavaConverters._

class LogProvider(client: KubernetesClient, namespace: String) {

  def podLogs(jobName: String): String = {
    val labelSelector = new LabelSelector
    labelSelector.setMatchLabels(Map("job-name" -> jobName).asJava)

    val podName = client.pods.inNamespace(namespace).withLabelSelector(labelSelector).list
      .getItems.asScala.maxBy(_.getStatus.getStartTime).getMetadata.getName

    client.pods().inNamespace(namespace).withName(podName).inContainer("build").getLog
  }
}
