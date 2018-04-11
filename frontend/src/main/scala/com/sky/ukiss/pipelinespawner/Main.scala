package com.sky.ukiss.pipelinespawner


import com.example.websocket.WebSocketsExample
import com.sky.ukiss.fruit.{FruitChooser => Fruit}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document

import scala.scalajs.js.annotation.JSExport

object Main {
  @JSExport
  def main(args: Array[String]): Unit = {
    val reactContainer = document.getElementById("main")

    <.div(
      <.h1("Hello!"),
      Fruit.Component(Fruit.Props(List(Fruit.Item(1, "Apple"), Fruit.Item(2, "Banana")))),
      <.h1("Web Socket Example"),
      JobList.WebSocketsApp()
    ).renderIntoDOM(reactContainer)
  }

}