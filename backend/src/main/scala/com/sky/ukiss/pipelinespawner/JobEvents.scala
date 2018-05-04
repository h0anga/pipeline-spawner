package com.sky.ukiss.pipelinespawner

import cats.effect.{Effect, IO}
import fs2.async.mutable.Queue
import io.fabric8.kubernetes.api.model.Job
import io.fabric8.kubernetes.client.Watcher.Action._
import io.fabric8.kubernetes.client.{KubernetesClient, KubernetesClientException, Watcher}
import org.http4s.websocket.WebsocketBits.{Text, WebSocketFrame}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Future
import fs2._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class MyJobEvents[F[_]](client: KubernetesClient,
                  namespace: String)(implicit F: Effect[F]) {
  def pushJobInfo() = Text(s"Something new")

  private val logger = LoggerFactory.getLogger(this.getClass)
  val jobsCell: mutable.Set[JobData] = mutable.Set()
  def jobs = jobsCell.seq

//  def toClientScheduledFromJobEvents0: Stream[IO, WebSocketFrame] = {
//    Scheduler[IO](corePoolSize = 2).flatMap(_.awakeEvery[IO](1.seconds).map(d => Text(s"Ping! $d")))
//    Stream[F, WebSocketFrame]
    //queues1
    //dequeue.map(e => Text("*** attempt to output smth"))
//    Scheduler[F](corePoolSize = 2).flatMap(_.awakeEvery[F](1.seconds).map(d => Text(s"Ping! $d")))
//  }

//  val streamData1: Stream[IO, String] = ???

  val k8sEventBuffer: Stream[IO, Queue[IO, String]] = Stream.eval(async.circularBuffer[IO, String](5))

  val element: Stream[IO, WebSocketFrame] =
    for {
      q <- k8sEventBuffer
      data <- q.dequeue
    } yield Text(data)

  val streamData: Stream[IO, WebSocketFrame] = Scheduler[IO](corePoolSize = 1).flatMap { scheduler =>
    scheduler.awakeEvery[IO](1.second).map(_ => Text((System.currentTimeMillis() % 10000).toString))
  }


  def dequeueData[IO[_]](q: Queue[IO, WebSocketFrame])(implicit F: Effect[IO]) = q.dequeue.take(4)

  def enqueueData[IO[_]](q: Queue[IO, WebSocketFrame])(implicit F: Effect[IO]): Stream[IO, Future[Unit]] =
    Stream.eval(
      F.delay(
        streamData
          .map(s => {
            async.unsafeRunAsync(q.enqueue1(s))( _ => IO.unit)
          })
          .compile
          .drain
          .unsafeToFuture()
      )
    )


  def toClientScheduledFromJobEvents: Stream[IO, WebSocketFrame] = {
    val queue: Stream[IO, Queue[IO, WebSocketFrame]] = Stream.eval(async.circularBuffer[IO, WebSocketFrame](5))

    queue.flatMap { q =>
      val enqueueStream = enqueueData(q)

      val dequeueStream = dequeueData(q)

      dequeueStream.concurrently(enqueueStream)
    }
  }

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
        println("*** Jobs changed: " + name)
//        k8sEventBuffer.flatMap(buffer => buffer.enqueue(Stream(jobData.name)))
      }
    })
  }

//object JobsChanged

case class JobData(name: String, app: String)