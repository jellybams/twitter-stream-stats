package spoolphiz.twitterstats.actor.stats

import akka.actor.{ActorRef, LoggingFSM, Props}
import com.danielasfregola.twitter4s.entities.Tweet
import spoolphiz.twitterstats.actor.stats.StatsProcessorActor._
import spoolphiz.twitterstats.service.StatsService

object StatsProcessorActor {
  def props(statsCollector: ActorRef) = Props(new StatsProcessorActor(statsCollector))

  /* states and state data */

  sealed trait ActorState

  sealed trait ActorData

  case object Ready extends ActorState

  case class NoData() extends ActorData

  /* messages and domain */

  /**
    * extracted stats for a single tweet
    */
  case class TweetStats(
                         emojiCount: Map[String, Int] = Map.empty,
                         hashtagCount: Map[String, Int] = Map.empty,
                         domainCount: Map[String, Int] = Map.empty,
                         containsImage: Boolean = false
                       )

  case class TweetStatsEnvelope(stats: TweetStats, sender: ActorRef)

}

class StatsProcessorActor(statsCollector: ActorRef) extends LoggingFSM[ActorState, ActorData] {

  startWith(Ready, NoData())

  when(Ready) {
    case Event(t: Tweet, _: NoData) => tweetToStats(t)
  }

  /**
    * transforms a `Tweet` into a `TweetStats` and sends the latter to the stats
    * collector actor for aggregation and serving
    *
    * @param tweet
    * @return
    */
  private def tweetToStats(tweet: Tweet) = {
    log.debug("processing tweet text: [{}]", tweet.text)

    val domainCounts = StatsService.extractDomainCounts(tweet)

    statsCollector ! TweetStats(
      emojiCount = StatsService.extractEmojis(tweet.text),
      hashtagCount = StatsService.extractHashtags(tweet.entities),
      domainCount = domainCounts.map { case (k, v) => k.domain -> v },
      containsImage = domainCounts.keys.exists(_.isImage)
    )

    stay
  }

  initialize()
}
