
import java.io.File
import javax.security.auth.login.Configuration
import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import sun.rmi.server.Dispatcher
import scala.concurrent.duration._
import scala.io.Source




class Aggregate extends Actor {

  var countResult: List[Map[String, Int]] = List()

  def receive = {
    case countResultMap: Map[String, Int] => {
      countResult = countResult ++ List(countResultMap)
      val finalResult = countResult.fold(Map())((firstMap, secondMap) => firstMap ++ secondMap.map { case (key, value) => (key, value + firstMap.getOrElse(key, 0)) })
      println(finalResult)
    }
  }
}

class WordCounter extends Actor {

  def receive = {
    case file: String => {
      Factory.aggregate ! count(new File(file))
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

  implicit val timeout = Timeout(10.seconds)

  def receive = {
    case file: String => Factory.counter ! file
  }
}

object Factory {
  val system = ActorSystem("Start")
  val word = system.actorOf(Props[Words], "Word")
  val aggregate = system.actorOf(Props[Aggregate], "Aggregate")
  //routing
  var props = Props[WordCounter].withRouter(new RoundRobinPool(3))
  var counter = system.actorOf(props, "WordCounter")
}

object StartCount extends App {
  Factory.word ! "/home/knoldus/Music/b/one"
  Factory.word ! "/home/knoldus/Music/b/two"
  Factory.word ! "/home/knoldus/Music/b/three"
  Factory.system.terminate()
}
