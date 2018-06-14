import javax.servlet.ServletContext

import com.sky.ukiss.pipelinespawner.{SpawnerConfig, Context}
import org.scalatra.{LifeCycle, ScalatraServlet}

class ScalatraBootstrap extends LifeCycle {

  override def init(context: ServletContext) {
    val appContext = new Context(SpawnerConfig())
    try {
      context mount (appContext.webSocketComponent, "/ws")
      context mount (appContext.frontendRoute, "/static")
      context mount (appContext.gitHookServiceComponent, "/hook")
      context mount (appContext.logRoute, "/logs")
      context mount (appContext.statusRoute, "/status")
      context mount (new ScalatraServlet {
        get("/") {
          redirect("/static/content/index.html")
        }
      }, "/")
    } catch {
      case e: Throwable => e.printStackTrace()
    }
  }
}