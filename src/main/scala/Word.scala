
import java.io.File
import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.routing.{Router, RoundRobinRoutingLogic, ActorRefRoutee}
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await}
import scala.concurrent.duration._
import scala.io.Source

object Factory{
  val system = ActorSystem("Start")
  val word = system.actorOf(Props[Word], "Word")
  val counter = system.actorOf(Props[Counter], "Counter")
}

class Word extends Actor {
  implicit val timeout = Timeout(5.seconds)

  def count(file: File): Map[String, Int] = {
    val wordlist = Source.fromFile(file).mkString.split("\\W+").toList
    val wordmap = wordlist.groupBy(a => a)
    val x = wordmap.map { case (k, v) => (k, v.length) }
    x
  }

  def receive ={
    case file:String =>sender ! count(new File(file))
  }
}

class Counter extends Actor {
  implicit val timeout = Timeout(10.seconds)

  var router = {
    val routees = Vector.fill(3) {
      val r = context.actorOf(Props[Word])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive= {
    case file:String => {
      val result = router.route(file, sender())
      //val result = Await.result((Factory.word ? file), 10.seconds)
      println(result + file)
    }
  }
}

object Start extends App {
  Factory.counter ! "/home/knoldus/Music/b/one"
  Factory.counter ! "/home/knoldus/Music/b/two"
  Factory.counter ! "/home/knoldus/Music/b/three"
}
