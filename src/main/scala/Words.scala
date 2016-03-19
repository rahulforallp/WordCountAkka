
import java.io.File

import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source


class Aggregate extends Actor {

  def receive = {
    case countMap: Map[String, Int] => {
      println(countMap)
    }
  }
}

class Counter extends Actor {

  def receive = {
    case file: String => {
      //println(count(new File(file))+file)
      Factory.system.scheduler.scheduleOnce(50 second) {
        Factory.aggregate ! count(new File(file))
      }
    }
  }

  def count(file: File): Map[String, Int] = {
    val wordList = Source.fromFile(file).mkString.split("\\W+").toList
    val wordMap = wordList.groupBy(a => a)
    val x = wordMap.map { case (k, v) => (k, v.length) }
    x
  }
}

class Words extends Actor {

  implicit val timeout = Timeout(50.seconds)
  var router = {
    val routees = Vector.fill(3) {
      val r = context.actorOf(Props[Counter])
      context watch r
      ActorRefRoutee(r)
    }
    Router(RoundRobinRoutingLogic(), routees)
  }

  def receive = {
    case file: String => router.route(file, sender())
  }
}

object Factory {
  val system = ActorSystem("Start")
  val word = system.actorOf(Props[Words], "Word")
  val counter = system.actorOf(Props[Counter], "Counter")
  val aggregate = system.actorOf(Props[Aggregate], "Aggregate")
}

object Start extends App {
  Factory.word ! "/home/knoldus/Music/b/one"
  Factory.word ! "/home/knoldus/Music/b/two"
  Factory.word ! "/home/knoldus/Music/b/three"
  Factory.system.terminate()
}
