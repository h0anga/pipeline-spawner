package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.hooks.GithubPayload
import io.fabric8.kubernetes.api.model.Job
import io.fabric8.kubernetes.client.KubernetesClient

class KubernetesService(client: KubernetesClient,
                        namespace: String,
                        converter: GithubPayload => Job) {

  def onGitHook(hook: GithubPayload): String = {
    client.extensions().jobs()
      .inNamespace(namespace)
      .create(converter(hook))
      .getMetadata
      .getName
  }

}
