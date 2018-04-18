package com.sky.ukiss.pipelinespawner


import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

import scala.scalajs.js.annotation.JSExport

object Main {
  @JSExport
  def main(args: Array[String]): Unit = {
    val reactContainer = document.getElementById("main")

    <.div(
      <.h1("Current Jobs"),
      JobList.WebSocketsApp(JobList.Props("wss://echo.websocket.org"))
    ).renderIntoDOM(reactContainer)
  }
}