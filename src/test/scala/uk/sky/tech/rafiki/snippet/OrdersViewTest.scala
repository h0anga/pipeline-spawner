package uk.sky.tech.rafiki.snippet

import com.flextrade.jfixture.JFixtureSugar
import net.liftweb.common.Empty
import net.liftweb.http.{LiftSession, S}
import net.liftweb.util.Html5
import org.junit.runner.RunWith
import org.mockito.Mockito.when
import org.scalatest.junit.JUnitRunner
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, MustMatchers, Outcome}
import uk.sky.tech.rafiki.model.SimbaService
import uk.sky.ukiss.spawner.ProdConfiguration

import scala.collection.JavaConversions._

@RunWith(classOf[JUnitRunner])
class OrdersViewTest extends FlatSpec with MockitoSugar with MustMatchers with JFixtureSugar {

  val orders = mock[SimbaService]
  val allOrders = fixture[List[SimOrderRepresentation]]
  val newestOrder = allOrders.maxBy(_.getCreatedDateTime)
  val session = new LiftSession("", fixture[String], Empty)

  when(orders.getOrders) thenReturn allOrders

  ProdConfiguration.simbaService.default.set(orders)

  override def withFixture(test: NoArgTest): Outcome = {
    S.initIfUninitted(session) {
      test()
    }
  }

  val template = Html5.parse(getClass
    .getResourceAsStream("/jobs/view.html"))
    .openOrThrowException("could not load template")

  "the OrdersView" should "put the newest order on top" in {
    val rendered = OrdersView.render(template)
    val renderedRows = rendered.\\("table")
      .find(_.attribute("id").exists(_.text == "orders-table"))
      .get \ "tbody" \ "tr"

    renderedRows must have size 3
    val topRow = renderedRows.head
    topRow.attribute("id").get.text mustEqual "order-" + newestOrder.getId
    topRow.\("td").head.text mustEqual "" + newestOrder.getId

  }
}
