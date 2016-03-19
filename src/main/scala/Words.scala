
import java.io.File
import javax.security.auth.login.Configuration
import akka.actor.{Actor, ActorSystem, Props}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import sun.rmi.server.Dispatcher
import scala.concurrent.duration._
import scala.io.Source


my-dispatcher {
  # Dispatcher is the name of the event-based dispatcher
  type = Dispatcher
  # What kind of ExecutionService to use
    executor = "fork-join-executor"
  # Configuration for the fork join pool
    fork-join-executor {
      # Min number of threads to cap factor-based parallelism number to
        parallelism-min = 2
      # Parallelism (threads) ... ceil(available processors * factor)
      parallelism-factor = 2.0
      # Max number of threads to cap factor-based parallelism number to
        parallelism-max = 10
    }
  # Throughput defines the maximum number of messages to be
  # processed per actor before the thread jumps to the next actor.
  # Set to 1 for as fair as possible.
  throughput = 100
}

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
