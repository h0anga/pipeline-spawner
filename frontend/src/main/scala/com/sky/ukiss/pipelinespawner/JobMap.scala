package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, _}
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}

import scala.scalajs.js
import scala.util.{Failure, Success}

object JobMap {

  case class Props(url: String)

  case class State(
                    ws: Option[WebSocket], // TODO open websocket when component is created, and assume it's always there?
                    jobs: Map[JobId, JobData],
                    error: Option[String],
                    displayedJob: Option[(JobId, JobData)] = None
                  ) {

    def withMessage(line: String): State = {
      println(line)
      if (line == "X") return this // TODO why is this happening??? We're getting an X from the WebSocket!!!
      JobEvent.fromString(line) match {
        case Success(JobCreated(id, job)) => println("job created: " + job); copy(jobs = jobs + (id -> job), error = None)
        case Success(JobChanged(id, job)) => println("job updated: " + job); copy(jobs = jobs + (id -> job), error = None)
        case Success(NoJobEvent$) => println("Received the initial job event"); this
        case Success(other) => copy(error = Some(s"Unsupported event: $other"))
        case Failure(err) => copy(error = Some(
          s"""The message "$line" caused the following error: "${err.getMessage}".
         Here's the stack trace:
         ${err.getStackTrace.map(_.toString).mkString("\n")}"""
        ))
      }
    }
  }

  class Backend($: BackendScope[Props, State]) {
    def render(s: State) = {

      def jobContent(j: (JobId, JobData)) = {
          s.displayedJob.filter(_._1 == j._1).map(displayedJob =>
            <.tr(
              <.td(
                ^.colSpan := 2
              )(JobInfo.Component(displayedJob))
            )
          ).getOrElse(
            <.tr(
              <.td(j._1), <.td(j._2.appName), <.td(
                ^.color := "navy")
                (j._2.status)
            )
          )
      }

      <.div(
        <.table(
          ^.className := "table table-striped table-hover"
        )(
          <.thead(
            <.tr(
              <.th("ID"), <.th("Application Name"), <.th("Status")
            )
          ),
          <.tbody(
            s.jobs.map(j => jobContent(j)).toVector : _*
          )
        ),
        if (s.error.isDefined) {
          <.div(
            ^.className := "alert alert-danger",
            ^.role := "alert"
          )(
            s.error
          )
        } else {
          <.div()
        }
      )
    }

    def start(p: Props): Callback = {
      // This will establish the connection and return the WebSocket
      def connect = CallbackTo[WebSocket] {
        val direct = $.withEffectsImpure

        def onopen(e: Event): Unit = {
          // Indicate the connection is open
          direct.modState(s => {s.ws.foreach(_.send("Hello")); s})
        }

        def onmessage(e: MessageEvent): Unit = direct.modState(_.withMessage(e.data.toString))

        def onerror(e: Event): Unit = {
          val msg: String = e.asInstanceOf[js.Dynamic]
            .message.asInstanceOf[js.UndefOr[String]]
            .fold(s"Error occurred!")("Error occurred: " + _)
          direct.modState(_.copy(error = Some(msg)))
        }

        def onclose(e: CloseEvent): Unit = {
          direct.modState(_.copy(ws = None, error = Some(s"""Closed. Reason = "${e.reason}"""")))
        }

        // Create WebSocket and setup listeners
        val ws = new WebSocket(p.url)
        ws.onopen = onopen _
        ws.onclose = onclose _
        ws.onmessage = onmessage _
        ws.onerror = onerror _
        ws
      }

      // Here use attempt to catch any exceptions in connect
      connect.attempt.flatMap {
        case Right(ws) => $.modState(_.copy(ws = Some(ws)))
        case Left(error) => $.modState(_.copy(error = Some(s"Error connecting: ${error.getMessage}")))
      }
    }

    def end: Callback = {
      def closeWebSocket = $.state.map(_.ws.foreach(_.close())).attempt

      def clearWebSocket = $.modState(_.copy(ws = None))

      closeWebSocket >> clearWebSocket
    }
  }

  def Component = ScalaComponent.builder[Props]("Jobs")
    .initialState(State(None, Map.empty, None))
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.start($.props))
    .componentWillUnmount(_.backend.end)
    .build
}