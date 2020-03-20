package spoolphiz.twitterstats

import akka.actor.ActorSystem
import spoolphiz.twitterstats.actor.stats._
import spoolphiz.twitterstats.util.Logging

object Main extends App with Logging {
  log.info("starting twitter stream stats app")

  val system = ActorSystem("TwitterStatsStream")

  val statsCollectorActor = system.actorOf(
    StatsCollectorActor.props(),
    "stats-collector-actor"
  )

  val statsProcessorSupervisor = system.actorOf(
    StatsProcessorSupervisor.props(statsCollectorActor),
    "stats-processor-supervisor"
  )

  val streamSupervisor = system.actorOf(
    TwitterStreamSupervisor.props(statsProcessorSupervisor),
    "twitter-stream-supervisor")

  val statsPrinter = system.actorOf(
    StatsConsolePrinter.props(statsCollectorActor),
    "stats-console-printer"
  )
}
