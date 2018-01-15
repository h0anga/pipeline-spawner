package com.sky.ukiss.spawner.krr

import java.io.StringWriter
import javax.servlet.http.HttpServletResponse

import com.sky.ukiss.spawner.ProdConfiguration
import io.prometheus.client.exporter.common.TextFormat
import net.liftweb.http.PlainTextResponse
import net.liftweb.http.rest.RestHelper

object Metrics extends RestHelper {
  serve {
    case Get("metrics" :: Nil, _) =>
      val writer = new StringWriter()
      TextFormat.write004(writer, ProdConfiguration.collectorRegistry.vend.metricFamilySamples())
      PlainTextResponse(writer.toString, List("Content-Type" -> TextFormat.CONTENT_TYPE_004), HttpServletResponse.SC_OK)
  }
}
