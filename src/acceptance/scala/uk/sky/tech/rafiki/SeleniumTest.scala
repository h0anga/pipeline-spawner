package uk.sky.tech.rafiki

import org.apache.commons.lang3.SystemUtils
import org.junit.runner.RunWith
import org.openqa.selenium.{By, WebDriver}
import org.openqa.selenium.chrome.ChromeDriver
import org.scalatest.junit.JUnitRunner
import org.scalatest.selenium.WebBrowser
import org.scalatest.{FeatureSpec, GivenWhenThen, MustMatchers}

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class SeleniumTest extends FeatureSpec with MustMatchers with WebBrowser with GivenWhenThen {

  // TODO change for pipeline-spawner.

  // One can use something like this to test the hook
  val hookJson = """
{
  "before": "95790bf891e76fee5e1747ab589903a6a1f80f22",
  "after": "da1560886d4f094c3e6c9ef40349f7d38b5d27d7",
  "ref": "refs/heads/master",
  "user_id": 4,
  "user_name": "John Smith",
  "project_id": 15,
  "repository": {
    "name": "Diaspora",
    "url": "git@example.com:diaspora.git",
    "description": "",
    "homepage": "http://example.com/diaspora"
  },
  "commits": [
    {
      "id": "b6568db1bc1dcd7f8b4d5a946b0b91f9dacd7327",
      "message": "Update Catalan translation to e38cb41.",
      "timestamp": "2011-12-12T14:27:31+02:00",
      "url": "http://example.com/diaspora/commits/b6568db1bc1dcd7f8b4d5a946b0b91f9dacd7327",
      "author": {
        "name": "Jordi Mallach",
        "email": "jordi@softcatala.org"
      }
    },
    {
      "id": "da1560886d4f094c3e6c9ef40349f7d38b5d27d7",
      "message": "fixed readme",
      "timestamp": "2012-01-03T23:36:29+02:00",
      "url": "http://example.com/diaspora/commits/da1560886d4f094c3e6c9ef40349f7d38b5d27d7",
      "author": {
        "name": "GitLab dev user",
        "email": "gitlabdev@dv6700.(none)"
      }
    }
  ],
  "total_commits_count": 4
}
    """

  if (SystemUtils.IS_OS_WINDOWS) {
    System.setProperty("webdriver.chrome.driver", "lib/chromedriver.exe")
  } else if (SystemUtils.IS_OS_LINUX) {
    System.setProperty("webdriver.chrome.driver", "lib/chromedriver")
  } else {
    throw new RuntimeException("Unsupported platform " + SystemUtils.OS_NAME)
  }

  implicit val webDriver: WebDriver = new ChromeDriver()

  val baseUrl = "http://localhost:8080"

  feature("Not all users are allowed to create orders") {
    info(
      """
        As a supply chain person, I want to be the only one able to buy SIMs,
        but my colleagues in the warehouse can look at existing orders.
      """)

    scenario("A warehouse person is logged in") {
      Given("A user with a short password logs in")
      performNewLogin("1234")

      userIsInViewOrdersPage

      Then("There is no link to create a new order")
      find(id("new-order-link")) must be(None)
    }

    scenario("A person who can buy SIMs is logged in") {
      Given("A user with a long password is logged in")
      performNewLogin("long password")

      userIsInViewOrdersPage

      Then("There is a link to create a new order")
      val newOrderLink = find(id("new-order-link"))
      newOrderLink mustBe defined

      When("The user clicks on that link")
      click on newOrderLink.get

      Then("The Create New Order page is displayed")
      pageTitle must include("New Order")
    }
  }

  feature("Order list and details") {
    scenario("A user looks at existing orders") {
      Given("A user is logged in")
      performNewLogin("1234")

      userIsInViewOrdersPage

      Then("The order ID is in the first column")
      val orderRow = find(id("order-1")).get.underlying.findElements(By.tagName("td"))
      orderRow(0).getText must be("1")

      Then("The IMSI Start is in the third column")
      orderRow(2).getText must be("1")

      Then("The IMSI End is in the fourth column")
      orderRow(3).getText must be("100000")

      When("The user click on the link in the last column")
      click on orderRow.last.findElement(By.tagName("a"))

      Then("The page shows the order details for that order")
      currentUrl must endWith("orders/details/1")

      Then("The batch number is in the first column")
      val batchRow = find(id("batch-1")).get.underlying.findElements(By.tagName("td"))
      batchRow(0).getText must be("1")

      Then("The batch IMSI start is in the second column")
      batchRow(1).getText must be("1")

      Then("The batch IMSI end is in the third column")
      batchRow(2).getText must be("100000")

      Then("The batch state is in the fourth column")
      batchRow(3).getText must be("MANUFACTURING_REQUESTED")
    }
  }

  private def userIsInViewOrdersPage = {
    When("The user is in the view orders page")
    pageTitle must endWith("Orders")
  }

  def performNewLogin(password: String): Unit = {
    go to baseUrl + "/login"
    find(id("btnLogOut")).foreach(click on _)

    click on "textName"
    enter("test-user")
    click on "textPassword"
    enter(s"$password\n")
  }
}
