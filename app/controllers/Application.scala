package controllers

import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class Application extends Controller {

  def index = Action { implicit request =>
    Redirect(controllers.sample.routes.EventSearch.index)
  }

}
