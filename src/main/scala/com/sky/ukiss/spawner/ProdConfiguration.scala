package com.sky.ukiss.spawner

import net.liftweb.util.SimpleInjector
import sky.sns.hermes.status.service.{StatusService, StatusServiceConfig, StatusServiceImpl}

object ProdConfiguration extends SimpleInjector {
  val statusServiceConfig = new Inject[StatusServiceConfig](() => new StatusServiceConfig.Builder("0.0.1").build()) {}
  val statusService = new Inject[StatusService](() => new StatusServiceImpl(statusServiceConfig.vend)) {}
}
