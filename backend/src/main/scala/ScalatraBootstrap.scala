import javax.servlet.ServletContext

import com.sky.ukiss.pipelinespawner.Context
import org.scalatra.LifeCycle

class ScalatraBootstrap extends LifeCycle {

  override def init(context: ServletContext) {
    val appContext = new Context
    try {
      context mount (appContext.frontendRoute, "/*")
      context mount (appContext.gitHookServiceComponent, "/*")
      context mount (appContext.webSocketComponent, "/*")
    } catch {
      case e: Throwable => e.printStackTrace()
    }
  }
}