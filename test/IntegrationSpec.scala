import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import com.gargoylesoftware.htmlunit.WebClient

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
@RunWith(classOf[JUnitRunner])
class IntegrationSpec extends Specification {

  /**
   * irgendwas is mit dem HtmlUnit zusammen mit dem JQuery web jar kaputt ..
   * com.gargoylesoftware.htmlunit.scriptexception typeerror
   * http://grokbase.com/t/gg/play-framework/135ehp97m9/2-1-1-why-do-integrationspec-hang
   */
  //  "Application" should {
  //
  //    "work from within a browser" in new WithBrowser {
  //
  //      browser.goTo("http://localhost:" + port + "/gw/start")
  //
  //      browser.pageSource must contain("Gateway2 Akka")
  //    }
  //
  //    "show data browser" in new WithBrowser {
  //
  //      browser.goTo("http://localhost:" + port + "/gw/inventorynodes")
  //
  //      browser.pageSource must contain("Inventory NODES")
  //
  //      browser.pageSource must contain("0012100000000001")
  //    }
  //  }
}
