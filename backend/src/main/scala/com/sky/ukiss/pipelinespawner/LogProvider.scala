package com.sky.ukiss.pipelinespawner

import io.fabric8.kubernetes.api.model.LabelSelector
import io.fabric8.kubernetes.client.KubernetesClient
import scala.collection.JavaConverters._

class LogProvider(client: KubernetesClient) {

   def podLogs(jobName: String): String = {
    val labelSelector = new LabelSelector
    labelSelector.setMatchLabels(Map("job-name" -> jobName).asJava)

    val podNames = client.pods.withLabelSelector(labelSelector).list
      .getItems.asScala.map(p => p.getMetadata.getName)

    podNames
      .map(podName => s"$podName :\n ${client.pods.withName(podName).getLog}")
      .mkString("\n\n")
  }

}
