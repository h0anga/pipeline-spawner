package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api.{JobData, JobId}
import japgolly.scalajs.react
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, CallbackTo}
import org.scalajs.dom.html.Div
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}

import scala.scalajs.js

object JobInfo {

  case class Props(id: JobId, data: JobData)

  case class State(logs: Seq[String] = Seq(), ws: Option[WebSocket] = None)

  class Backend($: BackendScope[Props, State]) {

    def render(state: State, props: Props): VdomTagOf[Div] = {
//      def refreshLogs(): Callback = Callback.future {
//        val url = Global.webUrl + "/logs/" + props.id
//        println(s"getting logs from $url")
//        for {
//          logs <- Ajax.get(url).map(_.responseText)
//        } yield $.setState(logs)
//      }

      <.div(
        <.h1(
          ^.className := "display-4"
        )(
          props.data.appName
        ),
        <.p(
          ^.className := "lead"
        )(
          s"Job ${props.id}"
        ),
        <.div(
          <.pre(state.logs.mkString("\n"))
        )
      )
    }

    def start(p: Props): Callback = {
      // This will establish the connection and return the WebSocket
      def connect = CallbackTo[WebSocket] {
        val direct = $.withEffectsImpure

        def onopen(e: Event): Unit = {
          // Indicate the connection is open
          direct.modState(s => {
            s.ws.foreach(_.send("Hello"));
            s
          })
        }

        def onmessage(e: MessageEvent): Unit = {
          val line = e.data.toString
          println(line)
          if (line == "X") return // TODO why is this happening??? We're getting an X from the WebSocket!!!
          direct.modState(s => s.copy(logs = s.logs :+ line))
        }

        def onerror(e: Event): Unit = {
          val msg: String = e.asInstanceOf[js.Dynamic]
            .message.asInstanceOf[js.UndefOr[String]]
            .fold(s"Error occurred!")("Error occurred: " + _)
          direct.modState(_.copy(logs = Seq("error:", msg)))
        }

        def onclose(e: CloseEvent): Unit = {
          direct.modState(s => s.copy(logs = s.logs :+ s"""Closed. Reason = "${e.reason}"""", ws = None))
        }

        // Create WebSocket and setup listeners
        val ws = new WebSocket(Global.webSocketUrl + "/ws/logs/" + p.id)
        ws.onopen = onopen _
        ws.onclose = onclose _
        ws.onmessage = onmessage _
        ws.onerror = onerror _
        ws
      }

      // Here use attempt to catch any exceptions in connect
      connect.attempt.flatMap {
        case Right(ws) => $.modState(_.copy(ws = Some(ws)))
        case Left(error) => $.modState(_.copy(logs = Seq(s"Error connecting: ${error.getMessage}")))
      }
    }

    def end: Callback = {
      def closeWebSocket = $.state.map(_.ws.foreach(_.close())).attempt

      def clearWebSocket = $.modState(_.copy(ws = None))

      closeWebSocket >> clearWebSocket
    }
  }

  def Component = react.ScalaComponent.builder[Props]("JobInfo")
    .initialState(State())
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.start($.props))
    .componentWillUnmount(_.backend.end)
    .build

}
