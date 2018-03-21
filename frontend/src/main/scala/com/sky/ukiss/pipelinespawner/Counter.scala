package com.sky.ukiss.pipelinespawner


import org.scalajs.dom
import org.scalajs.dom._
import scala.scalajs.js
import scala.scalajs.js.Any._
import scala.scalajs.js.Dynamic.{literal, global => g}
import scala.scalajs.js.{Dynamic, JSApp}
import scala.scalajs.js.annotation.JSExport
import scalajs.vuejs.Vue

object Counter {

  //defines the type for the data in the main Vue instance
  @js.native
  trait Data extends Vue {
    var count: Int = js.native
  }

  //set type for Vue methods in main instance with no parameters
  type VueMethod=js.ThisFunction0[Data,_]

  //Counter as component
  Vue.component("counter", literal(
    data = () => {
      literal(
        count = 0
      )
    },
    methods = literal(
      increment = ((data:Data) =>
        data.count += 1
        ):VueMethod
    ),
    template = """<div> {{count}}
      <button @click='increment'>Increment</button>
      </div>"""
  ))

  @JSExport
  def main(args: Array[String]) = {
    //main Vue instance to attach to #main
    val app = new Vue(
      literal(
        el = "#main"
      )
    )
  }
}
