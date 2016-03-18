
import java.io.File

import akka.actor.{Actor, ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.{ Await}
import scala.concurrent.duration._
import scala.io.Source


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

object Factory{
  val system = ActorSystem("Start")
  val word = system.actorOf(Props[Word], "Word")
  val counter = system.actorOf(Props[Counter], "Counter")
}

class Counter extends Actor {
  implicit val timeout = Timeout(10.seconds)
  def receive= {
    case "start" =>val result = Await.result((Factory.word ? "/home/knoldus/Music/b/one"), 10.seconds)
      println(result)
  }
}

object Start extends App {
Factory.counter ! "start"
}
