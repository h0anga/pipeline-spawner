package com.sky.ukiss.spawner.snippet

import com.sky.ukiss.spawner.ProdConfiguration
import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._

object ViewAllJobs {
  lazy val menu = Menu.i("Jobs") / "jobs" / "view"

  lazy val events = ProdConfiguration.jobEvents.vend

  def render = "#jobs-list" #>
    events.currentJobs.map(job =>
      ".job-name *" #> job.name &
      ".app-name *" #> job.app
    )
}
