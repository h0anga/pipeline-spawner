package com.sky.ukiss.spawner.jobs

import com.sky.ukiss.spawner.ProdConfiguration
import io.fabric8.kubernetes.api.model.Job
import io.fabric8.kubernetes.client.Watcher.Action.ADDED
import io.fabric8.kubernetes.client.{KubernetesClient, KubernetesClientException, Watcher}
import net.liftweb.common.Func
import net.liftweb.http.S
import net.liftweb.util.ValueCell

import scala.collection.JavaConverters._
import scala.collection.parallel.mutable

object JobEvents {
  private val client: KubernetesClient = ProdConfiguration.kubernetes.vend
  private val namespace: String = ProdConfiguration.defaultNamespace

  val jobs = ValueCell(Jobs)  //mutable.ParSet[JobData]()

  Jobs ++= client.extensions().jobs()
      .inNamespace(namespace)
      .list().getItems.asScala
      .map((j: Job) => JobData(j.getMetadata.getName, j.getMetadata.getLabels.get("app_name")))
      .toSet

  client.extensions().jobs().inNamespace(namespace).watch(new Watcher[Job] {
    override def onClose(cause: KubernetesClientException): Unit = ???

    override def eventReceived(action: Watcher.Action, job: Job): Unit = {
      val name = job.getMetadata.getName
      val labels = job.getMetadata.getLabels
      println(s"${action.name()} job $name with labels $labels")

      action match {
        case ADDED =>
          Jobs += JobData(name, labels.get("app_name"))
          println("Added the new job")
        case _ =>
          println("TODO do something else")
      }

      S.session.foreach(_.sendCometMessage(JobChanged))
    }
  })
}

object JobChanged