package com.sky.ukiss.spawner.krr

import com.sky.ukiss.spawner.ProdConfiguration
import net.liftweb.http.rest.RestHelper

object Metrics extends RestHelper {
  serve {
    case Get("metrics" :: Nil, _) => ProdConfiguration.statusService
  }
}
