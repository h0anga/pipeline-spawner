package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api.{JobData, JobId}
import japgolly.scalajs.react
import japgolly.scalajs.react.{BackendScope, Callback}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.ext.Ajax
import org.scalajs.dom.html.Div

import scala.concurrent.ExecutionContext.Implicits.global

object JobInfo {

  case class Props(id: JobId, data: JobData)

  type State = String

  class Backend($: BackendScope[Props, State]) {

    def render(state: State, props: Props): VdomTagOf[Div] = {
      def refreshLogs(): Callback = Callback.future {
        val url = Global.webUrl + "/logs/" + props.id
        println(s"getting logs from $url")
        for {
          logs <- Ajax.get(url).map(_.responseText)
        } yield $.setState(logs)
      }

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
        <.button(
          ^.`type` := "button",
          ^.className := "btn btn-primary"
        )(
          ^.onClick --> refreshLogs(),
          "Get logs"
        ),
        <.div(
          <.pre(state)
        )
      )
    }
  }

  def Component = react.ScalaComponent.builder[Props]("JobInfo")
    .initialState("")
    .renderBackend[Backend]
    .build

}
