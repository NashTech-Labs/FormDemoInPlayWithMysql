package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

import org.fluentlenium.core.filter.FilterConstructor._

class IntegrationSpec extends Specification {

  "Application" should {

    "work from within a browser" in {
      running(TestServer(3333), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:3333/")

        browser.$("header h1").first.getText must equalTo("Play 2.0 Form Demo — MySql")
        browser.$("div ul li a").click()
        browser.$("fieldset legend").first.getText must equalTo("Form Demo in Play2.0 With Mysql As Database")

      }
    }

  }

}