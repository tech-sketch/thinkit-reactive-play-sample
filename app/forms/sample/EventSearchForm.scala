package forms.sample

import play.api.data._
import play.api.data.Forms._
import java.sql.Date

case class EventSearchData(
  eventId: Option[String],
  eventNm: Option[String],
  eventDate: Option[Date])

trait EventSearchForm {
  val eventSearchForm = Form(
    mapping(
      "eventId" -> optional(text(maxLength = 100)),
      "eventNm" -> optional(text(maxLength = 100)),
      "eventDate" -> optional(sqlDate)
      )(EventSearchData.apply)(EventSearchData.unapply))
}