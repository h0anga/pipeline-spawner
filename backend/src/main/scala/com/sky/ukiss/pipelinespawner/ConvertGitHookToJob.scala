package com.sky.ukiss.pipelinespawner

import java.io.ByteArrayInputStream
import java.time.Clock

import com.sky.ukiss.pipelinespawner.hooks.GithubPayload
import com.sky.ukiss.pipelinespawner.utils.Utils._
import io.fabric8.kubernetes.api.model._
import io.fabric8.kubernetes.client.KubernetesClient

class ConvertGitHookToJob(generateId: () => String,
                          clock: Clock,
                          kubernetesClient: KubernetesClient) extends (GithubPayload => Job) {

  private val repo = "repo.sns.sky.com:8186"
  private val version = "1.0.13"
  private val buildImage = s"$repo/dost/pipeline-build:$version"
  private val myName = "pipeline-spawner"

  override def apply(hook: GithubPayload): Job = {
    val id = generateId()
    val jobName = s"$myName-$id"
    val appBeingBuilt = hook.repository.name
    val now = formattedTimestamp(clock.instant())
    val cloneUrl = hook.project.map(_.git_http_url).getOrElse(hook.repository.url)
    val commit = hook.after

    val jobYaml =
      s"""
apiVersion: batch/v1
kind: Job
metadata:
  labels:
    app_building: $appBeingBuilt
    app_name: $myName
  name: $jobName
spec:
  backoffLimit: 0
  template:
    metadata:
      labels:
        app_building: $appBeingBuilt
        app_name: $myName
      name: $jobName
    spec:
      restartPolicy: Never
      containers:
      - image: $buildImage
        name: build
        command:
        - bash
        - -c
        - git clone $cloneUrl application && cd application/pipeline && git checkout $commit && make build push
        env:
        - name: ARTIFACTORY_USERNAME
          valueFrom:
            secretKeyRef: 
              name: pipeline-spawner-secret
              key: artifactoryUser
        - name: ARTIFACTORY_PASSWORD
          valueFrom:
            secretKeyRef:
              name: pipeline-spawner-secret
              key: artifactoryUser
        - name: GO_PIPELINE_LABEL
          value: "$now"
        volumeMounts:
        - name: secret-volume
          mountPath: /build/.ssh
        - name: docker-exec-volume
          mountPath: /usr/bin/docker
        - name: docker-sock
          mountPath: /var/run
      volumes:
      - name: secret-volume
        secret:
          defaultMode: 420
          secretName: pipeline-spawner-secret
      - name: docker-exec-volume
        hostPath:
          path: /var/run/pipeline-spawner/docker
      - name: docker-sock
        hostPath:
          path: /var/run
      """

    kubernetesClient.extensions().jobs().load(new ByteArrayInputStream(jobYaml.getBytes)).get()
  }

}

