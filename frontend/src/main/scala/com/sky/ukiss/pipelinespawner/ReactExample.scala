import japgolly.scalajs.react.{BackendScope, Callback}
import japgolly.scalajs.react.extra.components.TriStateCheckbox
import japgolly.scalajs.react._
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom
import org.scalajs.dom.{document, html}

import scala.scalajs.js.annotation.JSExport

object ReactExample {

  case class Item(id: Int, name: String)

  case class Props(items: List[Item]) {
    val ids: Set[Int] = items.map(_.id)(collection.breakOut)
    val size: Int = items.size
  }

  type State = Set[Int]

  class Backend($: BackendScope[Props, State]) {

    def render(p: Props, s: State) = {

      def allCheckbox = {
        val triState =
          s.size match {
            case 0 => TriStateCheckbox.Unchecked
            case n if n < p.size => TriStateCheckbox.Indeterminate
            case _ => TriStateCheckbox.Checked
          }

        def setNextState: Callback =
          $.setState(
            triState.nextDeterminate match {
              case TriStateCheckbox.Checked => p.ids
              case TriStateCheckbox.Unchecked => Set.empty
            }
          )

        TriStateCheckbox.Props(triState, setNextState).render
      }

      def allRow =
        <.label(
          ^.display.block,
          ^.borderBottom := "1px solid",
          ^.width := "15ex",
          allCheckbox,
          "All Fruit")

      def itemCheckbox(id: Int) = {
        val on = s.contains(id)

        def toggle = if (on) s - id else s + id

        <.input.checkbox(
          ^.checked := on,
          ^.onChange --> $.setState(toggle))
      }

      def itemRow(item: Item) =
        <.label(
          ^.display.block,
          ^.fontWeight.normal,
          itemCheckbox(item.id),
          item.name)

      <.div(allRow)(p.items.map(itemRow): _*)
    }

    // When an item is removed from props, remove it from state as well.
    def onPropsChange(newProps: Props): Callback =
      $.modState(_ intersect newProps.ids)
  }

  lazy val App = ScalaComponent.builder[Props]("TriStateCheckboxExample")
    .initialState[State](Set.empty)
    .renderBackend[Backend]
    .componentWillReceiveProps(i => i.backend.onPropsChange(i.nextProps))
    .build

  @JSExport
  def main(args: Array[String]): Unit = {
    val reactContainer = document.createElement("div")
    document.body.appendChild(reactContainer)

    <.div(
      <.h1("Hello!"),
      App(Props(List(Item(1, "Apple"), Item(2, "Banana"))))
    ).renderIntoDOM(reactContainer)
  }
}