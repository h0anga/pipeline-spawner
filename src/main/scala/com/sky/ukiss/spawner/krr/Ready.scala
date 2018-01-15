package com.sky.ukiss.spawner.krr

import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST

object Ready extends RestHelper {
  serve {
    case Get("ready"::Nil, _) => JsonAST.JNothing
  }
}
