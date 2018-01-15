package com.sky.ukiss.spawner.krr

import com.sky.ukiss.spawner.ProdConfiguration
import net.liftweb.http.rest.RestHelper
import net.liftweb.json.JsonAST._
import sky.sns.hermes.status.service.StatusRepresentation

import scala.collection.JavaConverters._

object Status extends RestHelper {
  serve {
    case Get( "status" :: Nil, _) => toJson(ProdConfiguration.statusService.vend.getStatus)
  }

  def toJson(s: StatusRepresentation): JObject = JObject(
    JField("applicationVersion", JString(s.getApplicationVersion)),
    JField("overallStatus", JString(s.getOverallStatus.toString)),
    JField("probes", JArray(s.getProbes.asScala.toList.map(probe => JObject(
      JField("status", JString(probe.getStatus.toString)),
      JField("description", JString(probe.getDescription)),
      JField("name", JString(probe.getName)),
    )))),
    JField("optionalValues", JObject(s.getOptionalValues.asScala.map{
      case (k, v) => JField(k, JString(v.toString))
    }.toList))
  )
}
