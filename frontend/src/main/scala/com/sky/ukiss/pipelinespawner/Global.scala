package com.sky.ukiss.pipelinespawner

object Global {
  import JsGlobals._
  def webSocketUrl = s"$webSocketProtocol://$serverAddress"
  def webUrl = s"$webProtocol://$serverAddress"
}
