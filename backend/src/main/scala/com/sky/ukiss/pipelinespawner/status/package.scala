package com.sky.ukiss.pipelinespawner
import io.circe.Json


package object status {

  case class StatusPage(applicationVersion: String,
                        probes: Seq[Probe],
                        optionalValues: Map[String, String]) {
    def overallStatus: ProbeStatus.Value = probes.map(_.status).max

    def toJson: String = {
      import io.circe.syntax._
      this.asJson.spaces2
    }
  }
  import io.circe.Encoder
  import io.circe.generic.semiauto.deriveEncoder
  import io.circe.syntax._

  implicit val probeStatusEncoder: Encoder[ProbeStatus.Value] = Encoder.enumEncoder(ProbeStatus)
  implicit val statusEncoder: Encoder[StatusPage] = (status: StatusPage) => Json.obj(
    "applicationVersion" -> Json.fromString(status.applicationVersion),
    "overallStatus" -> status.overallStatus.asJson,
    "probes" -> status.probes.asJson,
    "optionalValues" -> status.optionalValues.asJson
  )
  implicit val probeEncoder: Encoder[Probe] = deriveEncoder

  object ProbeStatus extends Enumeration {
    val OK, WARN, ERROR = Value
  }

  case class Probe(name: String, description: String, status: ProbeStatus.Value)

}
