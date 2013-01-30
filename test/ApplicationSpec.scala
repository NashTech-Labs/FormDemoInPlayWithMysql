import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.test.FakeRequest
import play.api.test
import play.api.test.Helpers._

@RunWith(classOf[JUnitRunner])
class ApplicationSpec extends Specification {
  
  "index" in {
    val result = controllers.Application.index(FakeRequest())
    status(result) must equalTo(OK)
    contentType(result) must beSome("text/html")
  }
  
   "authenticateUser" in {
    val result = controllers.Application.authenticateUser(FakeRequest())
    status(result) must equalTo(400)
    contentAsString(result) must contain("There is some error")
    contentType(result) must beSome("text/html")
  }

}