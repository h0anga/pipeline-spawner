package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api.Job
import io.circe.parser.decode
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, CallbackOption, _}
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}

import scala.scalajs.js

object JobList {
  case class Props(url: String)

  case class State(ws: Option[WebSocket], jobs: Vector[Job], error: Option[String], message: String) {
    def allowSend: Boolean =
      ws.exists(_.readyState == WebSocket.OPEN) && message.nonEmpty

    // Create a new state with a line added to the log
    def withMessage(line: String): State = {
      import io.circe.generic.auto._

      decode[Job](line) match {
        case Right(job) => copy(jobs = jobs :+ job, error = None)
        case Left(err) => copy(error = Some(err.getMessage))
      }
    }
  }

  class Backend($: BackendScope[Props, State]) {
    def render(s: State) = {

      // Can only send if WebSocket is connected and user has entered text
      val send: Option[Callback] =
        for (ws <- s.ws if s.allowSend)
          yield sendMessage(ws, s.message)

      def sendOnEnter(e: ReactKeyboardEvent): Callback =
        CallbackOption.asEventDefault(e,
          CallbackOption.keyCodeSwitch(e) {
            case KeyCode.Enter => send.getOrEmpty
          }
        )

      <.div(
        <.p("Enter a Job in Json format and watch it rendered in the list below:"),
        <.form(
          ^.className := "form-inline"
        )(
          <.div(
            ^.className := "form-group",
            <.input.text(
              ^.className := "form-control",
              ^.autoFocus := true,
              ^.value := s.message,
              ^.onChange ==> onChange,
              ^.onKeyDown ==> sendOnEnter),
          ),
          <.button(
            ^.classSet("btn" -> true,  "btn-primary" -> true),
            ^.disabled := send.isEmpty, // Disable button if unable to send
            ^.onClick -->? send, // --> suffixed by ? because it's for Option[Callback]
            "Send")
        ),
        <.h3("Jobs"),
        <.ul( // Display jobs
          s.jobs.map(j => <.li(j.name)): _*
        ),
        <.div(
          ^.classSet("bg-danger" -> s.error.isDefined, "visible" -> s.error.isDefined)
        )(
          s.error
        )
      )
    }

    private def onChange(e: ReactEventFromInput) = {
      val newMessage = e.target.value // NB don't inline `newMessage`, or it will stop working
      $.modState(_.copy(message = newMessage))
    }

    private def sendMessage(ws: WebSocket, msg: String) = Callback(ws.send(msg)) >> $.modState(s => s.copy(message = ""))

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

  def WebSocketsApp = ScalaComponent.builder[Props]("WebSocketsApp")
    .initialState(State(None, Vector.empty, None, """{"id":0, "name": "MyJob"}"""))
    .renderBackend[Backend]
    .componentDidMount($ => $.backend.start($.props))
    .componentWillUnmount(_.backend.end)
    .build
}