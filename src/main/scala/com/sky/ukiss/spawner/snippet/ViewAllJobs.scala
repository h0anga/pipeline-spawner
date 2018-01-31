package com.sky.ukiss.spawner.snippet

import com.sky.ukiss.spawner.jobs.JobEvents
import net.liftweb.http.WiringUI
import net.liftweb.sitemap.Menu
import net.liftweb.util.Helpers._

object ViewAllJobs {
  lazy val menu = Menu.i("Jobs") / "jobs" / "view"

  def render = "#jobs-list" #>
    WiringUI(JobEvents.jobs) { jobs =>
      "tbody" #>
        jobs.toSeq.sortBy(_.name).map(job => "tr" #> (
          ".job-name *" #> job.name &
            ".app-name *" #> job.app
          ))
    }
}
