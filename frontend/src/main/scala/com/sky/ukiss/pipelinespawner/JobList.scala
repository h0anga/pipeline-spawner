package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api.Job
import io.circe
import io.circe.parser.decode
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, CallbackOption, _}
import org.scalajs.dom.ext.KeyCode
import org.scalajs.dom.{CloseEvent, Event, MessageEvent, WebSocket}

import scala.scalajs.js

object JobList {
  val url = "wss://echo.websocket.org"

  case class State(ws: Option[WebSocket], jobs: Vector[Job], error: Option[String], message: String) {

    def allowSend: Boolean =
      ws.exists(_.readyState == WebSocket.OPEN) && message.nonEmpty

    // Create a new state with a line added to the log
    def log(line: String): State = {
      import io.circe.generic.auto._

      val decoded: Either[circe.Error, Job] = decode[Job](line)
      decoded match {
        case Right(job) => copy(jobs = jobs :+ job, error = None)
        case Left(err) => copy(error = Some(err.getMessage))
      }
    }
  }

  class Backend($: BackendScope[Unit, State]) {
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
        <.h3("Type a message and get an echo:"),
        <.div(
          <.input.text(
            ^.autoFocus := true,
            ^.value := s.message,
            ^.onChange ==> onChange,
            ^.onKeyDown ==> sendOnEnter),
          <.button(
            ^.disabled := send.isEmpty, // Disable button if unable to send
            ^.onClick -->? send, // --> suffixed by ? because it's for Option[Callback]
            "Send")),
        <.h4("Connection log"),
        <.ul(  // Display log
          s.jobs.map(j => <.li(j.name)): _*
        ),
        <.div(
          ^.classSet("error" -> s.error.isDefined, "visible" -> s.error.isDefined)
        )(
            s.error
        )
      )
    }

    def onChange(e: ReactEventFromInput): Callback = {
      val newMessage = e.target.value
      $.modState(_.copy(message = newMessage))
    }

    def sendMessage(ws: WebSocket, msg: String): Callback = {
      // Send a message to the WebSocket
      def send = Callback(ws.send(msg))

      // Update the log, clear the text box
      def updateState = $.modState(s => s.log(s.message).copy(message = ""))

      send >> updateState
    }

    def start: Callback = {

      // This will establish the connection and return the WebSocket
      def connect = CallbackTo[WebSocket] {

        // Get direct access so WebSockets API can modify state directly
        // (for access outside of a normal DOM/React callback).
        // This means that calls like .setState will now return Unit instead of Callback.
        val direct = $.withEffectsImpure

        // These are message-receiving events from the WebSocket "thread".

        def onopen(e: Event): Unit = {
          // Indicate the connection is open
          direct.modState(_.log("Connected."))
        }

        def onmessage(e: MessageEvent): Unit = {
          // Echo message received
          direct.modState(_.log(s"Echo: ${e.data.toString}"))
        }

        def onerror(e: Event): Unit = {
          // Display error message
          val msg: String =
            e.asInstanceOf[js.Dynamic]
              .message.asInstanceOf[js.UndefOr[String]]
              .fold(s"Error occurred!")("Error occurred: " + _)
          direct.modState(_.log(msg))
        }

        def onclose(e: CloseEvent): Unit = {
          // Close the connection
          direct.modState(_.copy(ws = None).log(s"""Closed. Reason = "${e.reason}""""))
        }

        // Create WebSocket and setup listeners
        val ws = new WebSocket(url)
        ws.onopen = onopen _
        ws.onclose = onclose _
        ws.onmessage = onmessage _
        ws.onerror = onerror _
        ws
      }

      // Here use attempt to catch any exceptions in connect
      connect.attempt.flatMap {
        case Right(ws) => $.modState(_.log(s"Connecting to $url ...").copy(ws = Some(ws)))
        case Left(error) => $.modState(_.log(s"Error connecting: ${error.getMessage}"))
      }
    }

    def end: Callback = {
      def closeWebSocket = $.state.map(_.ws.foreach(_.close())).attempt

      def clearWebSocket = $.modState(_.copy(ws = None))

      closeWebSocket >> clearWebSocket
    }
  }

  val WebSocketsApp = ScalaComponent.builder[Unit]("WebSocketsApp")
    .initialState(State(None, Vector.empty, None, """ {"id":0, "name": "MyJob"} """))
    .renderBackend[Backend]
    .componentDidMount(_.backend.start)
    .componentWillUnmount(_.backend.end)
    .build
}