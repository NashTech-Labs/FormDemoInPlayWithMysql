import play.api._
import play.api.mvc.Results.InternalServerError
import play.api.Logger
import play.api.mvc.RequestHeader
import play.api.mvc.SimpleResult
import play.api.mvc.Result

object Global extends GlobalSettings {

  override def onStart(app: Application): Unit = {
    println("********************************************************************************")
    Logger.info("Application has started")
  }

  override def onStop(app: Application): Unit = {
    Logger.info("Application shutdown...")
  }

  
  
}
