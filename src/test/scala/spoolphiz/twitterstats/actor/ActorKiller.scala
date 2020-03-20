package spoolphiz.twitterstats.actor

import akka.actor.{ActorRef, PoisonPill, Terminated}
import akka.testkit.{TestKit, TestProbe}

import scala.concurrent.duration._

trait ActorKiller {
  this: TestKit =>

  val actorTerminationWatcher = TestProbe()

  def terminateActor(ref: ActorRef): Unit = {
    actorTerminationWatcher.watch(ref)
    ref ! PoisonPill
    actorTerminationWatcher.expectMsgType[Terminated](3 seconds)
    actorTerminationWatcher.unwatch(ref)
  }
}
