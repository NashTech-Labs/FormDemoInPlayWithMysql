package models

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Employee(id: Pk[Int] = NotAssigned, email: String, password: String)
case class EmployeeDetail(id: Pk[Int] = NotAssigned, employeeId: Option[Int], name: String, designation: String, address: String, contact_no: String)

object Employee {

  /**
   * Parse a Employee from a ResultSet
   */
  val simple = {
    get[Pk[Int]]("employee.employee_id") ~
      get[String]("employee.email") ~
      get[String]("employee.password") map {
        case id ~ email ~ password => Employee(id, email, password)
      }
  }

  /**
   * Parse a (Employee,EmployeeDetail) from a ResultSet
   */
  val withEmployeeDetail = Employee.simple ~ (EmployeeDetail.simple ?) map {
    case employee ~ employeeDetail => (employee, employeeDetail)
  }

  /**
   * Register a new employee.
   *
   * @param employee The computer values.
   */
  def insert(employee: Employee): Int = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into EMPLOYEE(EMAIL,PASSWORD) values (
            {email}, {password}
          )
        """).on(
          'email -> employee.email,
          'password -> employee.password).executeUpdate()
    }
  }

  /**
   * Find Employee Via Email Id
   *
   * @param email the employee email id.
   */
  def findByEmployeeEmail(email: String) = {
    DB.withConnection { implicit connection =>
      val employees = SQL(
        """
          select * from EMPLOYEE 
          where EMAIL = {email}
        """).on(
          'email -> email).as(Employee.simple.*)
      employees
    }
  }
  /**
   * Find Employee Via Employee Id
   *
   * @param id the employee  id.
   */
  def findByEmployeeId(id: Int) = {
    DB.withConnection { implicit connection =>
      val employeeFound = SQL(
        """
          select * from EMPLOYEE 
          where EMPLOYEE_ID = {id}
        """).on(
          'id -> id).as(Employee.simple.singleOpt)
      employeeFound
    }
  }

  /**
   * Find Max Employee Id
   */
  def findMaxEmployeeId = {
    DB.withConnection { implicit connection =>
      val empId = SQL(
        """
          select MAX(EMPLOYEE_ID) from EMPLOYEE 
        """).as(scalar[Int].single)
      empId
    }
  }

  /**
   * Find Employee Via Email and password
   */
  def authenticate(employee: Employee) = {
    DB.withConnection { implicit connection =>
      val employeeFound = SQL(
        """
          select * from EMPLOYEE 
          where EMAIL = {email} and PASSWORD= {password}
        """).on(
          'email -> employee.email,
          'password -> employee.password).as(Employee.simple.singleOpt)
      employeeFound
    }
  }

  /**
   * Delete All Employees.
   */
  def delete = {
    DB.withConnection { implicit connection =>
      SQL("delete from EMPLOYEE").executeUpdate()
    }
  }

  /**
   * Find Employee With Employee Detail
   */

  def employeeDetail(employeeId: Int) = {
    DB.withConnection { implicit connection =>
      val employeeDetail = SQL(
        """
          select * from EMPLOYEE_DETAIL
          where EMPLOYEE_ID = {employeeId}
        """).on(
          'employeeId -> employeeId).as(EmployeeDetail.simple.singleOpt)
      employeeDetail
    }
  }

}

object EmployeeDetail {

  /**
   * Parse a Employeedetail from a ResultSet
   */
  val simple = {
    get[Pk[Int]]("employee_detail.employee_detail_id") ~
      get[Option[Int]]("employee_detail.employee_id") ~
      get[String]("employee_detail.name") ~
      get[String]("employee_detail.designation") ~
      get[String]("employee_detail.address") ~
      get[String]("employee_detail.contact_no") map {
        case id ~ employeeId ~ name ~ designation ~ address ~ contact_no =>
          EmployeeDetail(id, employeeId, name, designation, address, contact_no)
      }
  }

  /**
   * Register a new employee.
   *
   * @param employee The computer values.
   */
  def insert(employeeDetail: EmployeeDetail): Int = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          insert into EMPLOYEE_DETAIL(EMPLOYEE_ID ,NAME,DESIGNATION,ADDRESS,CONTACT_NO) values (
            {employeeId}, {name},{designation},{address},{contact_no}
          )
        """).on(
          'employeeId -> employeeDetail.employeeId.get,
          'name -> employeeDetail.name,
          'designation -> employeeDetail.designation,
          'address -> employeeDetail.address,
          'contact_no -> employeeDetail.contact_no).executeUpdate()
    }
  }

  /**
   * Update a employee detail.
   *
   * @param employeeDetail is EmployeeDetail
   */
  def update(employeeDetail: EmployeeDetail) = {
    DB.withConnection { implicit connection =>
      SQL(
        """
          update EMPLOYEE_DETAIL
          set NAME = {name}, DESIGNATION = {designation}, ADDRESS = {address}, CONTACT_NO = {contact_no}
          where EMPLOYEE_ID = {employeeId}
        """).on(
          'employeeId -> employeeDetail.employeeId.get,
          'name -> employeeDetail.name,
          'designation -> employeeDetail.designation,
          'address -> employeeDetail.address,
          'contact_no -> employeeDetail.contact_no).executeUpdate()
    }
  }

}