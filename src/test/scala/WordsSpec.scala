//
//import akka.actor.ActorSystem
//import akka.actor.Actor
//import akka.actor.Props
//import akka.testkit.{ TestActors, TestKit, ImplicitSender }
//import org.scalatest.WordSpecLike
//import org.scalatest.Matchers
//import org.scalatest.BeforeAndAfterAll
//
///**
//  * Created by knoldus on 18/3/16.
//  */
//class WordsSpec (_system: ActorSystem) extends TestKit(_system) with ImplicitSender
//  with WordSpecLike with Matchers with BeforeAndAfterAll {
//
//
//  def this() = this(ActorSystem("WordsSpec"))
//
//  override def afterAll {
//    TestKit.shutdownActorSystem(system)
//  }
//
//  "An Counter actor" must {
//
//    "send back message" in {
//      val echo = system.actorOf(TestActors.echoActorProps)
//      echo ! "/home/knoldus/Music/b/one"
//      expectMsg("Map(well -> 1, hi -> 1, hello -> 1, good -> 1)")
//    }
//
//  }
//
//}
