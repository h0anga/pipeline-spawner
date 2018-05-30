package com.sky.ukiss.pipelinespawner

import java.io.ByteArrayInputStream
import java.time.Clock

import com.sky.ukiss.pipelinespawner.hooks.GithubPayload
import com.sky.ukiss.pipelinespawner.utils.Utils._
import io.fabric8.kubernetes.api.model._
import io.fabric8.kubernetes.client.KubernetesClient

class ConvertGitHookToJob(generateId: () => String,
                          clock: Clock,
                          kubernetesClient: KubernetesClient,
                          myName: String) extends (GithubPayload => Job) {

  private val repo = "repo.sns.sky.com:8186"
  private val version = "1.0.13"
  private val buildImage = s"$repo/dost/pipeline-build:$version"

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
        - sleep 5; sudo chmod a+rw /var/run/docker.sock && git clone $cloneUrl application && cd application/pipeline && git checkout $commit && make push
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
        - name: docker-exec-location
          mountPath: /usr/local/bin
        - name: build-dir
          mountPath: /build/application
        - name: var-run
          mountPath: /var/run
      - name: dind-daemon
        image: docker:18.05.0-dind
        args: ["--insecure-registry", "repo.sns.sky.com:8186", "--insecure-registry", "repo.sns.sky.com:8185", "-D"]
        resources:
          requests:
            cpu: 20m
            memory: 512Mi
        lifecycle:
          postStart:
            exec:
              command: [cp, /usr/local/bin/docker, /var/run/pipeline-spawner/docker]
        securityContext:
          privileged: true
        volumeMounts:
          - name: docker-graph-storage
            mountPath: /var/lib/docker
          - name: docker-exec-location
            mountPath: /var/run/pipeline-spawner
          - name: plugins
            mountPath: /run/docker/plugins/
          - name: json-store
            mountPath: /var/lib/docker/plugin-data/
          - name: data-volume
            mountPath: /data
          - name: var-run
            mountPath: /var/run
      - name: local-volume-plugin
        image: cwspear/docker-local-persist-volume-plugin:v1.3.0
        volumeMounts:
          - name: plugins
            mountPath: /run/docker/plugins/
          - name: json-store
            mountPath: /var/lib/docker/plugin-data/
          - name: data-volume
            mountPath: /data
      volumes:
      - name: secret-volume
        secret:
          defaultMode: 420
          secretName: pipeline-spawner-secret
      - name: docker-graph-storage
        emptyDir: {}
      - name: plugins
        emptyDir: {}
      - name: json-store
        emptyDir: {}
      - name: data-volume
        emptyDir: {}
      - name: docker-exec-location
        emptyDir: {}
      - name: build-dir
        emptyDir: {}
      - name: var-run
        emptyDir: {}
      """

    println(jobYaml)

    kubernetesClient.extensions().jobs().load(new ByteArrayInputStream(jobYaml.getBytes)).get()
  }

}

