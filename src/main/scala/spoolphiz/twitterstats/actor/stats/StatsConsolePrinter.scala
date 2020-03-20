package spoolphiz.twitterstats.actor.stats

import java.text.DecimalFormat

import akka.actor.{Actor, ActorRef, Props}
import spoolphiz.twitterstats.actor.stats.StatsCollectorActor.GetStats
import spoolphiz.twitterstats.domain.StatsData

import scala.concurrent.duration._

object StatsConsolePrinter {
  def props(statsCollector: ActorRef) = Props(new StatsConsolePrinter(statsCollector))
}

class StatsConsolePrinter(statsCollector: ActorRef) extends Actor {

  import context.dispatcher

  context.system.scheduler.schedule(4 seconds, 3 seconds, self, GetStats)

  private val formatter = new DecimalFormat("#.00")

  override def receive: Receive = {
    case GetStats => statsCollector ! GetStats
    case data: StatsData => output(data)
  }

  private def output(data: StatsData): Unit = {
    val output =
      s"""
        | =========================================================
        | total tweets processed: ${data.totalTweets}
        | total run time (sec): ${data.runTimeSecs}
        | average tweets per second: ${data.averageTweets.second}
        | average tweets per minute: ${data.averageTweets.minute}
        | average tweets per hour: ${data.averageTweets.hour}
        | top emojis: ${data.topEmojis.map{ case (k, v) => k + " -> " + v}.mkString("  ")}
        | tweets with emoji: ${data.tweetsWithEmoji}
        | tweets with emoji (%): ${formatter.format(data.tweetsWithEmojiPercent)}%
        | top hashtags: ${data.topHashtags.map{ case (k, v) => k + " -> " + v}.mkString("  ")}
        | tweets with url (%): ${formatter.format(data.tweetsWithUrlPercent)}%
        | tweets with image url (%): ${formatter.format(data.tweetsWithImageUrlPercent)}%
        | top domains: ${data.topDomains.map{ case (k, v) => k + " -> " + v}.mkString("  ")}
        | =========================================================
        |
      """.stripMargin

    println(output)
  }
}
