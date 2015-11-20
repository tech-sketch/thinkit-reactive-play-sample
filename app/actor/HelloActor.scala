package actor

import akka.actor._

object HelloActor {
  def props(out: ActorRef) = Props(new HelloActor(out))
}

class HelloActor(out: ActorRef) extends Actor {
  // Actorがメッセージを受信した時の処理
  def receive = {
    case msg: String =>
      // 「Hello XXX」という文字列を返す
      out ! (s"Hello $msg")
  }
}