package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api._
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, _}
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}

import scala.scalajs.js
import scala.util.{Failure, Success}

object JobList {

  case class Props(url: String)

  case class State(
                    ws: Option[WebSocket], // TODO open websocket when component is created, and assume it's always there?
                    jobs: Vector[Job],
                    error: Option[String],
                    displayedJob: Option[Job] = None
                  ) {

    def withMessage(line: String): State = {
      println(line)
      if (line == "X") return this // TODO why is this happening??? We're getting an X from the WebSocket!!!
      JobEvent.fromString(line) match {
        case Success(JobCreated(job)) => println("job created: " + job); copy(jobs = jobs :+ job, error = None)
        case Success(NoJobEvent) => println("Received the initial job event"); this
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
      <.div(
        <.table(
          ^.className := "table table-striped table-hover"
        )(
          <.thead(
            <.tr(
              <.th("ID"), <.th("Name")
            )
          ),
          <.tbody(
            s.jobs.map(j => {
              s.displayedJob.filter(_.id == j.id).map(displayedJob =>
                <.tr(
                  <.td(
                    ^.colSpan := 2
                  )(JobInfo.Component(displayedJob))
                )
              ).getOrElse(
                <.tr(
                  <.td(j.id), <.td(j.name)
                )
              )
            }): _*
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
          direct.modState(s => s) // this is basically a noop, but it I don't do it, it doesn't work.
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
    .initialState(State(None, Vector.empty, None))
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.start($.props))
    .componentWillUnmount(_.backend.end)
    .build
}