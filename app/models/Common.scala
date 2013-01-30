package models

import play.api.Play
import anorm._

/**
 * class to show alert
 * @param alertType the Alert Type
 * @param message the message on Alert
 */
case class Alert(alertType: String,
  message: String)

object Common {

  var alert: Alert = new Alert("", "")
  def setAlert(alert: Alert): Unit = this.alert = alert


}