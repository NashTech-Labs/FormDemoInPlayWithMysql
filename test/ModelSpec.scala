package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._
import anorm._
import org.specs2.mutable.BeforeAfter
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.db.DB
import play.api.Play.current

@RunWith(classOf[JUnitRunner])
class ModelSpec extends Specification with BeforeAfter {

  import models._
  override def before {
    running(FakeApplication()) {
      DB.withConnection {
        implicit connection =>
          println("++++++" + Employee.delete)
          Employee.delete
      }
    }
  }

  "save employee" in {
    evolutionFor("default")
    running(FakeApplication()) {
      DB.withConnection {
        implicit connection =>
          Employee.delete
          val employee = Employee(NotAssigned, "ruchi@knoldus.com", "123456")
          Employee.insert(employee)
          val employeeList = SQL("select * from EMPLOYEE").as(Employee.simple *)
          employeeList.size must equalTo(1)
          val employee_Id = Employee.findMaxEmployeeId
          val employeeRegistered = Employee.findByEmployeeId(employee_Id).get
          employeeRegistered.email must equalTo("ruchi@knoldus.com")
      }
    }
  }

  "find employee by email id" in {
    evolutionFor("default")
    running(FakeApplication()) {
      DB.withConnection {
        implicit connection =>
          Employee.delete
          val employee = Employee(NotAssigned, "ruchi@knoldus.com", "123456")
          Employee.insert(employee)
          val employees = Employee.findByEmployeeEmail("ruchi@knoldus.com")
          employees.size must equalTo(1)
      }
    }
  }

  "find employee by employee id" in {
    evolutionFor("default")
    running(FakeApplication()) {
      DB.withConnection {
        implicit connection =>
          Employee.delete
          val employee = Employee(NotAssigned, "ruchi@knoldus.com", "123456")
          Employee.insert(employee)
          val employee_Id = Employee.findMaxEmployeeId
          val employeeFound = Employee.findByEmployeeId(employee_Id)
          employeeFound.get.email must equalTo("ruchi@knoldus.com")
      }
    }
  }

  "authenticate employee" in {
    evolutionFor("default")
    running(FakeApplication()) {
      DB.withConnection {
        implicit connection =>
          Employee.delete
          val employee = Employee(NotAssigned, "ruchi@knoldus.com", "123456")
          Employee.insert(employee)
          val employeeFound = Employee.authenticate(employee)
          employeeFound.get.email must equalTo("ruchi@knoldus.com")
      }
    }
  }

  override def after {
    running(FakeApplication()) {
      DB.withConnection {
        implicit connection =>
          Employee.delete
          println("++++++after" + Employee.delete)
          val employeeList = SQL("select * from EMPLOYEE").as(Employee.simple *)
          println("++++++after size" + employeeList.size)
      }
    }
  }

}