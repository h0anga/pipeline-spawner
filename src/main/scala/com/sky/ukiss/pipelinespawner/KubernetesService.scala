package com.sky.ukiss.pipelinespawner

import io.fabric8.kubernetes.api.model.Job
import io.fabric8.kubernetes.client.KubernetesClient

class KubernetesService(client: KubernetesClient,
                        namespace: String,
                        converter: GitHookPayload => Job) {

  def onGitHook(hook: GitHookPayload): String = {
    client.extensions().jobs()
      .inNamespace(namespace)
      .create(converter(hook))
      .getMetadata
      .getName
  }

}
