package com.sky.ukiss.spawner.jobs

import net.liftweb.sitemap.Menu

object ViewAll {
  lazy val menu = Menu.i("Jobs") / "jobs" / "view"

  // view all jobs spawned by us
  def render = <b>success!</b>
}
