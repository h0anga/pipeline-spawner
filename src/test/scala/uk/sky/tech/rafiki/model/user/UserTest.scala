package uk.sky.tech.rafiki.model.user

import com.flextrade.jfixture.JFixtureSugar
import net.liftweb.common.Empty
import net.liftweb.http.{LiftSession, S}
import org.scalatest.{FlatSpec, MustMatchers, Outcome}
import uk.sky.tech.rafiki.model.user.User.{CreateOrders, ViewOrders}

class UserTest extends FlatSpec with MustMatchers with JFixtureSugar {

  override def withFixture(test: NoArgTest): Outcome = {
    val session = new LiftSession("", fixture[String], Empty)
    S.initIfUninitted(session) {
      test()
    }
  }

  "A User with the CreateOrders role" should "be able to view and create orders" in {
      User.roles.set(Set(CreateOrders))

      User.canViewOrders must be(true)
      User.canCreateOrders must be(true)
  }

  "A User with the ViewOrders role" should "be able to view, but not create, orders" in {
      User.roles.set(Set(ViewOrders))

      User.canViewOrders must be(true)
      User.canCreateOrders must be(false)
  }
}
