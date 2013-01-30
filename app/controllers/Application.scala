package controllers

import play.api._
import play.api.mvc._
import play.api.data.Forms
import play.api.data.Form
import play.api.data.Forms._
import anorm._
import models.Employee
import models.Common
import models.Alert
import models.EmployeeDetail
import play.api.data.validation.Constraints._

object Application extends Controller {

  /**
   * Login In Form
   */
  val signInForm = Form(
    Forms.mapping(
      "id" -> ignored(NotAssigned: Pk[Int]),
      "email" -> email,
      "password" -> nonEmptyText(minLength = 6))(Employee.apply)(Employee.unapply))

  /**
   * Sign Up Form
   */
  val signupForm: Form[Employee] = Form(
    mapping(
      "email" -> email,
      "password" -> tuple(
        "main" -> text(minLength = 6),
        "confirm" -> text).verifying(
          //  Add an additional constraint: both passwords must match
          "Passwords don't match", passwords => passwords._1 == passwords._2)) {
        // Binding: Create a User from the mapping result (ignore the second password and the accept field)
        (email, passwords) => Employee(NotAssigned, email, passwords._1)
      } {
        // Unbinding: Create the mapping values from an existing User value
        user => Some(user.email, (user.password, ""))
      })

  /**
   * Employee Detail Form
   */
  val employeeDetailForm = Form(
    Forms.mapping(
      "id" -> ignored(NotAssigned: Pk[Int]),
      "employeeId" -> optional(number),
      "name" -> nonEmptyText,
      "designation" -> nonEmptyText,
      "address" -> nonEmptyText,
      "contact_no" -> (text(minLength = 10,maxLength=10) verifying pattern("""[0-9.+]+""".r, error = "A valid phone number is required")))(EmployeeDetail.apply)(EmployeeDetail.unapply))

  def index = Action {
    val alert: Alert = new Alert("", "")
    Common.setAlert(alert)
    Ok(views.html.index(signInForm, "Form Demo in Play2.0 With Mysql As Database"))
  }

  /**
   * Authenticate User For Login
   */
  def authenticateUser = Action { implicit request =>
    val alert: Alert = new Alert("", "")
    Common.setAlert(alert)
    signInForm.bindFromRequest.fold(
      errors => BadRequest(views.html.index(errors, "There is some error")),
      employee => {
        val employeeOpt = Employee.authenticate(employee)
        employeeOpt match {
          case None =>
            val alert: Alert = new Alert("error", "Invalid Credentials")
            Common.setAlert(alert)
            val invalidCredentialsForm = Application.signupForm.fill(Employee(NotAssigned, employee.email, ""))
            Ok(views.html.index(invalidCredentialsForm, "Invalid Credentials"))
          case Some(authemployee: Employee) =>
            val userSession = request.session + ("userId" -> authemployee.id.toString)
            val employeeDetailOpt = Employee.employeeDetail(authemployee.id.get)
            employeeDetailOpt match {
              case None => Ok(views.html.employeeDetail(Application.employeeDetailForm, authemployee)).withSession(userSession)
              case Some(employeeDetailFound: EmployeeDetail) =>
                val employeeDetailFormWithDetails = Application.employeeDetailForm.fill(employeeDetailFound)
                Ok(views.html.employeeDetail(employeeDetailFormWithDetails, authemployee)).withSession(userSession)
            }

        }
      })

  }

  /**
   * Redirect To Sign Up Page
   */
  def siginUpForm = Action {
    val alert: Alert = new Alert("", "")
    Common.setAlert(alert)
    Ok(views.html.signUpForm(signupForm, "Sign Up Form"))
  }

  /**
   * Register a new Employee
   */
  def createEmployee = Action { implicit request =>
    val alert: Alert = new Alert("", "")
    Common.setAlert(alert)
    signupForm.bindFromRequest.fold(
      errors => BadRequest(views.html.signUpForm(errors, "There is some error")),
      employee => {
        Employee.findByEmployeeEmail(employee.email).isEmpty match {
          case true =>
            Employee.insert(employee)
            val employee_Id = Employee.findMaxEmployeeId
            val employeeRegistered = Employee.findByEmployeeId(employee_Id).get
            val alert: Alert = new Alert("success", "Employee Registered")
            Common.setAlert(alert)
            val userSession = request.session + ("userId" -> employee_Id.toString)
            Ok(views.html.employeeDetail(Application.employeeDetailForm, employeeRegistered)).withSession(userSession)
          case false =>
            val alert: Alert = new Alert("error", "Email Id Already Exist")
            Common.setAlert(alert)
            val emailExistForm = Application.signupForm.fill(Employee(NotAssigned, employee.email, ""))
            Ok(views.html.signUpForm(emailExistForm, "Email Id Already Exist"))
        }

      })

  }

  def saveEmployeeDetail = Action { implicit request =>
    if (request.session.get("userId") == None) {
      Results.Redirect("/")
    } else {
      val employeeId = request.session.get("userId").get.toInt
      val employee = Employee.findByEmployeeId(employeeId).get
      employeeDetailForm.bindFromRequest.fold(
        errors => BadRequest(views.html.employeeDetail(errors, employee)),
        employeeDetail => {
          val employeeDetailOpt = Employee.employeeDetail(employee.id.get)
          employeeDetailOpt match {
            case None =>
              val updatedEmployeeDetail = EmployeeDetail(NotAssigned, Option(employeeId), employeeDetail.name,
                employeeDetail.designation, employeeDetail.address, employeeDetail.contact_no)
              EmployeeDetail.insert(updatedEmployeeDetail)
              val alert: Alert = new Alert("success", "Employee Details Saved")
              Common.setAlert(alert)
            case Some(employeeDetailFound: EmployeeDetail) =>
              val updatedEmployeeDetail = EmployeeDetail(employeeDetailFound.id, employeeDetailFound.employeeId, employeeDetail.name,
                employeeDetail.designation, employeeDetail.address, employeeDetail.contact_no)
              EmployeeDetail.update(updatedEmployeeDetail)
              val alert: Alert = new Alert("success", "Employee Details Updated")
              Common.setAlert(alert)
          }
          Results.Redirect("/employeeDetail")
        })
    }
  }

  /**
   * Redirect To employee detail page
   */

  def employeeDetail = Action { implicit request =>
    val employeeId = request.session.get("userId").get.toInt
    val employee = Employee.findByEmployeeId(employeeId).get
    val employeeDetailOpt = Employee.employeeDetail(employee.id.get)
    employeeDetailOpt match {
      case None => Ok(views.html.employeeDetail(Application.employeeDetailForm, employee))
      case Some(employeeDetailFound: EmployeeDetail) =>
        val formFill = Application.employeeDetailForm.fill(employeeDetailFound)
        Ok(views.html.employeeDetail(formFill, employee))
    }
  }
  /**
   * Log Out
   */

  def signout = Action {
    Common.setAlert(new Alert("success", "SignOut Successfully"))
    Results.Redirect("/").withNewSession
  }
}