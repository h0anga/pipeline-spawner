package com.sky.ukiss.spawner.comet

import com.sky.ukiss.spawner.jobs.{JobsChanged, JobEvents}
import net.liftweb.http.{CometActor, WiringUI}
import net.liftweb.sitemap.Menu

object ViewAllJobs {
  lazy val menu = Menu.i("Jobs") / "jobs" / "view"
}

class ViewAllJobs extends CometActor {
  def render = "#jobs-list" #>
    WiringUI(JobEvents.jobs) { jobs =>
      "tbody" #>
        jobs.toSeq.sortBy(_.name).map(job => "tr" #> (
          ".job-name *" #> job.name &
            ".app-name *" #> job.app
          ))
    }

  override def lowPriority: PartialFunction[Any, Unit] = {
    case JobsChanged =>
      println("*** RE-RENDERING")
      unregisterFromAllDependencies()
      theSession.clearPostPageJavaScriptForThisPage()
      reRender(true)
  }
}
