package com.sky.ukiss.pipelinespawner

import com.sky.ukiss.pipelinespawner.api.{JobData, JobId}
import japgolly.scalajs.react
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.html.Div

object JobInfo {
  type Props = (JobId, JobData)
  type State = Unit

  def render(state: State, props: Props): VdomTagOf[Div] = {
    <.div(
      <.h1(
        ^.className := "display-1"
      )(s"Job ${props._1}"),
      <.p(
        ^.className := "lead"
      )(
        props._2.appName
      ),
      <.div(
        <.pre(
          """
            |Ideally, job output will be printed here.
            |Lorem ipsum dolor sit abet. Ex falso quod libet.
          """.stripMargin
        )
      )
    )
  }

  def Component = react.ScalaComponent.builder[Props]("JobInfo")
    .initialState(())
    .noBackend
    .render($ => render($.state, $.props))
    .build

}
