package com.sky.ukiss.spawner.krr

import net.liftweb.http.rest.RestHelper

object Status extends RestHelper {
  serve {
    case Get( "status" :: Nil, _) => <b>status</b>
  }
}
