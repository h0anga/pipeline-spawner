package com.sky.ukiss.spawner.jobs

import com.sky.ukiss.spawner.ProdConfiguration
import io.fabric8.kubernetes.api.model.Job
import io.fabric8.kubernetes.client.Watcher.Action._
import io.fabric8.kubernetes.client.{KubernetesClient, KubernetesClientException, Watcher}
import net.liftweb.actor.LiftActor
import net.liftweb.common.{Box, Empty, Full}
import net.liftweb.http.{LiftSession, SessionInfo, SessionMaster, SessionWatcherInfo}
import net.liftweb.util.ValueCell
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._
import scala.collection.mutable

object JobEvents extends LiftActor{
  private var sessions: Box[Map[String, SessionInfo]] = Empty

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val client: KubernetesClient = ProdConfiguration.kubernetes.vend
  private val namespace: String = ProdConfiguration.defaultNamespace

  SessionMaster.sessionWatchers = SessionMaster.sessionWatchers ++ List(this)

  val jobsCell: ValueCell[mutable.Set[JobData]] = ValueCell(mutable.Set())

  def jobs = jobsCell.currentValue._1

  jobs ++= client.extensions().jobs()
      .inNamespace(namespace)
      .list().getItems.asScala
      .map((j: Job) => JobData(j.getMetadata.getName, j.getMetadata.getLabels.get("app_name")))
      .toSet

  client.extensions().jobs().inNamespace(namespace).watch(new Watcher[Job] {
    override def onClose(cause: KubernetesClientException): Unit = ???

    override def eventReceived(action: Watcher.Action, job: Job): Unit = {
      val name = job.getMetadata.getName
      val labels = job.getMetadata.getLabels
      val jobData = JobData(name, labels.get("app_name"))

      action match {
        case ADDED|MODIFIED => jobs += jobData
        case DELETED => jobs -= jobData
        case ERROR => logger.error("error about job events", job)
      }
      println("*** Jobs changed")

      sessions.foreach(_.values.foreach(_.session.sendCometMessage(JobsChanged)))
    }
  })

  override protected def messageHandler: PartialFunction[Any, Unit] = {
    case SessionWatcherInfo(s) =>
      printf(s"*** sessions changed: $s")
      sessions = Full(s)
  }
}

object JobsChanged