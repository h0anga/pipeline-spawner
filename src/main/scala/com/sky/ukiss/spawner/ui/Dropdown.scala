package com.sky.ukiss.spawner.ui

import net.liftweb.common.Empty
import net.liftweb.http.SHtml
import net.liftweb.http.js.JsCmds
import net.liftweb.util.{Helpers, PSettableValueHolder}

import scala.util.Try
import scala.xml.NodeSeq

/**
  * Create a Bootstrap dropdown
  * @param items The list of items in the dropdown
  * @param holder A `SessionVar` or `RequestVar` bound to the dropdown selection
  * @param renderItem Function to convert a `T` into a `NodeSeq` for display
  * @tparam T
  */
case class Dropdown[T](items: Seq[T], holder: PSettableValueHolder[T], renderItem: (T) => NodeSeq = { s: T => <span>{s}</span> })
  extends (NodeSeq => NodeSeq)
{
  val id = Helpers.randomString(10)

  def apply(src: NodeSeq) =
    <div class="dropdown dropdown-menu-left">
      <button class="btn btn-primary dropdown-toggle" role="button" data-toggle="dropdown">
        <span id={id}>
          {
          Try(holder.get)
            .filter(_!= Empty)
            .map(renderItem)
            .filter(_ != <span></span>)
            .getOrElse(src.text)
          }
        </span> <span class="caret"></span>
      </button>
      <ul class="dropdown-menu" id={id + "-list"}>
        {items.map( item =>
        <li>
          <a name="link" href="#" onClick={onItemSelected(item).toJsCmd}>
            {renderItem(item)}
          </a>
        </li>
      )}
      </ul>
    </div>

  private def onItemSelected(item: T) = SHtml.ajaxInvoke(() => {
    holder.set(item)
    JsCmds.SetHtml(id, renderItem(item))
  })
}
