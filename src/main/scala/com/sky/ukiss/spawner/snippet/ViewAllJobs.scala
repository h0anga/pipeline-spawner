package com.sky.ukiss.spawner.snippet

import com.sky.ukiss.spawner.jobs.{JobChanged, JobEvents}
import net.liftweb.http.{CometActor, WiringUI}
import net.liftweb.sitemap.Menu

import scala.xml.NodeSeq
import net.liftweb.util.Helpers.StringToCssBindPromoter

//TODO if I use CometActor, `render` changes signature and the view stops working!
object ViewAllJobs /*extends CometActor */{
  lazy val menu = Menu.i("Jobs") / "jobs" / "view"

  def render = "#jobs-list" #>
    WiringUI(JobEvents.jobs) { jobs =>
      "tbody" #>
        jobs.toSeq.sortBy(_.name).map(job => "tr" #> (
          ".job-name *" #> job.name &
            ".app-name *" #> job.app
          ))
    }

//  override def lowPriority: PartialFunction[Any, Unit] = {
//    case JobChanged =>
//      unregisterFromAllDependencies()
//      theSession.clearPostPageJavaScriptForThisPage()
//      reRender(true)
//  }
}
