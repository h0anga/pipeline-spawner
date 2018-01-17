package com.sky.ukiss.spawner.jobs

import com.sky.ukiss.spawner.ProdConfiguration
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST.JString

object GitHook extends RestHelper {
  serve {
    case JsonPost("hook" :: Nil, (body, _)) =>
      val hookData = body.extract[HookData]
      ProdConfiguration.hookToJob.vend.submit(hookData)
      JString("Cool, done!")
  }
}
