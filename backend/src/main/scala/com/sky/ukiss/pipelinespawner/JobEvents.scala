package com.sky.ukiss.pipelinespawner

import fs2.async.mutable.Queue
import io.fabric8.kubernetes.api.model.Job
import io.fabric8.kubernetes.client.Watcher.Action._
import io.fabric8.kubernetes.client.{KubernetesClient, KubernetesClientException, Watcher}
import org.http4s.websocket.WebsocketBits.{Text, WebSocketFrame}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable
import cats.effect.Effect
import fs2.{Pipe, Scheduler, async}
import org.http4s.server.websocket.WebSocketBuilder
import cats.implicits._
import cats.effect._
import cats.implicits._
import com.sky.ukiss.pipelinespawner.api.JobEvent
import fs2._
import fs2.StreamApp.ExitCode
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.websocket._
import org.http4s.websocket.WebsocketBits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class JobEvents[F[_]](client: KubernetesClient,
                  namespace: String)(implicit F: Effect[F]) {
  def pushJobInfo() = Text(s"Something new")

  private val logger = LoggerFactory.getLogger(this.getClass)
  val jobsCell: mutable.Set[JobData] = mutable.Set()
  def jobs = jobsCell.seq

  private var queues: mutable.MutableList[Queue[F, WebSocketFrame]] = mutable.MutableList()//[Queue[F, WebSocketFrame]]

  def addQueue(queue: Queue[F, WebSocketFrame]) = queues += queue

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
//        println("*** Jobs changed")
        queues.foreach(
          q => q.enqueue1(Text("""{"id": 0, "name": "from the backend"}"""))
//          _ => println("*** Jobs changed")
        )
//        val scheduler = Scheduler[F](corePoolSize = 2)
//        scheduler.awakeEvery[F](1.seconds).map(d => Text(s"Ping! $d"))

       // SessionMaster ! JobsChanged - ? notification to websocket

//        val queue: F[Queue[F, WebSocketFrame]] = async.unboundedQueue[F, WebSocketFrame]
//        val echoReply: Pipe[F, WebSocketFrame, WebSocketFrame] = _.collect {
//          case Text(msg, _) => Text("You sent the server: " + msg)
//          case _ => Text("Something new")
//        }
//
//        queue.flatMap { q =>
//          val d = q.dequeue.through(echoReply)
//          val e = q.enqueue
//          WebSocketBuilder[F].build(d, e)
//        }
      }
    })
  }

object JobsChanged

case class JobData(name: String, app: String)