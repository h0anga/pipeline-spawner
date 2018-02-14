package com.sky.ukiss.spawner.frontend.views.jobs

import io.udash.bootstrap.table.UdashTable
import io.udash.core.FinalView
import io.udash.css.CssView
import io.udash.properties.model.ModelProperty
import io.udash.properties.single.Property

class JobsView(model: ModelProperty[JobsModel], presenter: JobsPresenter) extends FinalView with CssView {

  import scalatags.JsDom.all._

  override def getTemplate = div(
    UdashTable(
      bordered = Property(true),
      hover = Property(true)
    )(model.subSeq(_.jobs))(
      headerFactory = Some(() => tr(th("Name"), th("ID")).render),
      rowFactory = job => {
        tr(
          td(job.get.name),
          td(job.get.id)
        ).render
      }
    ).render
  )
}
