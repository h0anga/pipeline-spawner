package com.sky.ukiss.spawner.krr

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JObject

object Metrics extends RestHelper {
  serve {
    case Get("metrics" :: Nil, _) => JObject()
  }
}
