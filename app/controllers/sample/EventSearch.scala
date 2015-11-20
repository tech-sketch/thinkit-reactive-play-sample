package controllers.sample

import java.sql.Date

import actor.HelloActor
import forms.sample.EventSearchForm
import models.Tables.{Event, EventRow}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import play.api.i18n.Messages.Implicits._
import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.libs.ws._
import play.api.mvc.{WebSocket, Action, Controller}
import play.api.Play
import play.api.Play.current
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import slick.backend.DatabasePublisher
import slick.driver.H2Driver.api._
import slick.driver.JdbcProfile

class EventSearch extends Controller with EventSearchForm with HasDatabaseConfig[JdbcProfile] {
  val dbConfig = DatabaseConfigProvider.get[JdbcProfile](Play.current)

  /**
   * EventSearchの初期表示
   * @return
   */
  def index = Action { implicit request =>
    Ok(views.html.sample.eventSearch(null))
  }

  /**
   * 通常のHTTPリクエストに対するControllerの実装
   * @return
   */
  def search1 = Action.async { implicit request =>
    // Eventの一覧（Seq[EventRow]）をFutureで取得
    val future: Future[Seq[EventRow]] = getEvents

    future.map { events =>
      Ok(views.html.sample.eventSearch(events))
    }
  }

  /**
   * WebSocketリクエストに対するControllerの実装
   * @return
   */
  def hi = WebSocket.using[String] { request =>
    // 受信メッセージは無視して、「Hi!」という文字列を返す
    val in = Iteratee.ignore[String]
    val out = Enumerator("Hi!")
    (in, out)
  }

  /**
   * WebSocketリクエストに対するActorを用いるControllerの実装
   * @return
   */
  def hello = WebSocket.acceptWithActor[String, String] { request => out =>
    // HelloActorの生成
    HelloActor.props(out)
  }

  /**
   * Reactive SlickがFutureを返す
   * @return
   */
  def search2 = Action.async { implicit request =>
    // コレクションを扱うようにfilterでeventNmが「ScalaMatsuri」のデータのみに絞り込む
    val query = Event.filter(_.eventNm === "ScalaMatsuri")
    val action = query.result

    // db.runを実行するとFutureを返す
    val future: Future[Seq[EventRow]] = db.run(action)

    // レスポンスはFutureのまま返す
    future.map { events =>
      Ok(views.html.sample.eventSearch(events))
    }
  }

  /**
   * Event一覧を全件取得
   * @return
   */
  def getEvents = db.run(Event.result)

  /**
   * Reactive SlickがストリーミングのFutureを返す
   * @return
   */
  def printByStream = {
    // 全Eventの一覧を取得
    val query = Event
    val action = query.result

    // Eventの一覧をDatabasePublisherで取得
    val dbPublisher: DatabasePublisher[EventRow] = db.stream(action)

    // 全イベント名を出力
    dbPublisher.foreach { event =>
      println(s"[printByStream] Event name: ${event.eventNm}")
    }
  }

  /**
   * データベース処理結果が返ってきた時の処理を定義する
   */
  def futureMap = {
    // 本日のイベントを取得するクエリ
    val query = Event.filter(_.eventDate === Date.valueOf("2016-01-30"))
    val action = query.result

    // db.runを実行するとFutureを返す
    val future: Future[Seq[EventRow]] = db.run(action)

    future.map { events =>
      // データベースからの応答が返ってきたら実行したい処理を定義
      events.map { event =>
        // イベント名を出力
        println(s"[futureMap] Event name: ${event.eventNm}")
      }
    }

    // データベースからの応答を待たずに実行したい処理を定義
    sendMail("futureMap")
  }

  /**
   * データベース処理の結果を待って処理をする
   */
  def futureAwait: Unit = {
    // 本日のイベントを取得するクエリ
    val query = Event.filter(_.eventDate === Date.valueOf("2016-01-30"))
    val action = query.result

    // db.runを実行するとFutureを返す
    val future: Future[Seq[EventRow]] = db.run(action)

    // データベースからの応答が返って来るまで（Duration.Inf）待つ
    val events = Await.result(future, Duration.Inf)
    events.map { event =>
      // イベント名を出力
      println(s"[futureAwait] Event name: ${event.eventNm}")
    }

    // データベースからの応答後に実行したい処理を定義
    sendMail("futureAwait")
  }

  /**
   * メールを送る（ダミー）
   */
  def sendMail(name: String) = {
    println(s"[${name}] Mail was sent")
  }

  /**
   * Webサービスを呼び出す
   * @return
   */
  def ws = Action.async { implicit request =>
    // Webサービスの呼び出し
    val wsRequest: WSRequest = WS.url("http://example.com/")
    val future: Future[WSResponse] = wsRequest.get()

    future.map { response =>
      Ok(response.body).as(HTML)
    }
  }

  /**
   * Stream、Future等のサンプル処理の呼び出し
   */
  def others = Action { implicit request =>
    println("-- printByStream --")
    printByStream
    println("-- futureMap --")
    futureMap
    println("-- futureAwait --")
    futureAwait
    Redirect(controllers.routes.Application.index)
  }

}
