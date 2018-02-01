package bootstrap.liftweb

import com.sky.ukiss.spawner.comet.ViewAllJobs
import com.sky.ukiss.spawner.jobs.GitHook
import com.sky.ukiss.spawner.krr.{Metrics, Ready, Status}
import net.liftweb.common._
import net.liftweb.http.ContentSourceRestriction.{Self, UnsafeEval, UnsafeInline}
import net.liftweb.http._
import net.liftweb.sitemap.Loc.{EarlyResponse, Hidden}
import net.liftweb.sitemap._

class Boot {
  def boot {

    // where to search snippet
    LiftRules.addToPackages("com.sky.ukiss.spawner")

    LiftRules.statelessDispatch.append(Status)
    LiftRules.statelessDispatch.append(Metrics)
    LiftRules.statelessDispatch.append(Ready)
    LiftRules.statelessDispatch.append(GitHook)

    // Build SiteMap
    def sitemap() = SiteMap(
      Menu.i("Home") / "index" >> Hidden >> EarlyResponse(() => S.redirectTo(ViewAllJobs.menu.loc.calcDefaultHref)),
      ViewAllJobs.menu
    )

    LiftRules.setSiteMapFunc(sitemap)

    LiftRules.sessionInactivityTimeout.default.set(Full(5 * 60 * 1000L))

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart = Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd = Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => true)

    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => Html5Properties(r.userAgent))

    // Silence the plethora of useless warnings from Lift 3.0
    val safeDefaults = List(Self, UnsafeInline, UnsafeEval)
    LiftRules.securityRules = () => SecurityRules(
      content = Some(ContentSecurityPolicy(defaultSources = safeDefaults, scriptSources = safeDefaults))
    )
  }
}
