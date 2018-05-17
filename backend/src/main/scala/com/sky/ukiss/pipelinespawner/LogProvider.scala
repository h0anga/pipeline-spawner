package com.sky.ukiss.pipelinespawner

import io.fabric8.kubernetes.api.model.LabelSelector
import io.fabric8.kubernetes.client.KubernetesClient
import scala.collection.JavaConverters._

class LogProvider(client: KubernetesClient, namespace: String) {

   def podLogs(jobName: String): String = {
    val labelSelector = new LabelSelector
    labelSelector.setMatchLabels(Map("job-name" -> jobName).asJava)

    val podNames = client.pods.inNamespace(namespace).withLabelSelector(labelSelector).list
      .getItems.asScala.map(p => p.getMetadata.getName)

    podNames
      .map(podName => s"$podName :\n ${client.pods.inNamespace(namespace).withName(podName).getLog}")
      .mkString("\n\n")
  }

}
