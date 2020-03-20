package spoolphiz.twitterstats.actor.stats

import akka.actor.{LoggingFSM, Props}
import spoolphiz.twitterstats.actor.stats.StatsCollectorActor._
import spoolphiz.twitterstats.actor.stats.StatsProcessorActor.TweetStats
import spoolphiz.twitterstats.domain.{StatsData, TweetCountAverages}
import spoolphiz.twitterstats.util.ConfigModule

object StatsCollectorActor {

  def props() = Props(new StatsCollectorActor)

  /* state and state data */

  sealed trait ActorState

  sealed trait ActorData

  case object ReadyState extends ActorState

  case class ReadyData(
                        startTime: Long = System.currentTimeMillis / 1000,
                        totalTweets: Int = 0,
                        allEmojis: Map[String, Int] = Map.empty,
                        topEmojis: Map[String, Int] = Map.empty,
                        allHashtags: Map[String, Int] = Map.empty,
                        topHashtags: Map[String, Int] = Map.empty,
                        allDomains: Map[String, Int] = Map.empty,
                        topDomains: Map[String, Int] = Map.empty,
                        tweetsWithEmoji: Double = 0.0,
                        tweetsWithUrl: Double = 0.0,
                        tweetsWithImageUrl: Double = 0.0
                      ) extends ActorData

  /* messages */

  case object GetStats

}

class StatsCollectorActor extends LoggingFSM[ActorState, ActorData] {

  private val numTopEntities = ConfigModule.app.getInt("actors.statsCollectorActor.numTopEntities")

  override def preStart() = {
    super.preStart()
    log.info("starting StatsCollectorActor")
  }

  startWith(ReadyState, ReadyData())

  when(ReadyState) {
    case Event(GetStats, data: ReadyData) =>
      log.debug("received GetStats request with state data {}", data)
      sender ! getStatsData(data)
      stay

    case Event(incoming: TweetStats, previous: ReadyData) => stay using updateStateData(incoming, previous)

    case _ =>
      log.info("got message")
      stay
  }

  private def getStatsData(data: ReadyData): StatsData = {
    val runTimeSecs = System.currentTimeMillis() / 1000 - data.startTime
    val averages = tweetCountAverages(data, runTimeSecs)

    StatsData(
      runTimeSecs = runTimeSecs,
      totalTweets = data.totalTweets,
      topEmojis = data.topEmojis,
      tweetsWithEmoji = data.tweetsWithEmoji,
      tweetsWithEmojiPercent = data.tweetsWithEmoji / data.totalTweets * 100,
      topHashtags = data.topHashtags,
      topDomains = data.topDomains,
      tweetsWithUrlPercent = data.tweetsWithUrl / data.totalTweets * 100,
      tweetsWithImageUrlPercent = data.tweetsWithImageUrl / data.totalTweets * 100,
      averageTweets = averages
    )
  }

  private def tweetCountAverages(data: ReadyData, runTimeSecs: Long): TweetCountAverages = {
    if (runTimeSecs == 0) TweetCountAverages()
    else TweetCountAverages(
      hour = (data.totalTweets / (runTimeSecs / 3600.0)).toInt,
      minute = (data.totalTweets / (runTimeSecs / 60.0)).toInt,
      second = (data.totalTweets / runTimeSecs).toInt
    )
  }

  private def updateStateData(incoming: TweetStats, data: ReadyData): ReadyData = {
    val updatedEmojiCounts = updateEntityCounts(incoming.emojiCount, data.allEmojis)
    val updatedHashtagCounts = updateEntityCounts(incoming.hashtagCount, data.allHashtags)
    val updatedDomainCounts = updateEntityCounts(incoming.domainCount, data.allDomains)

    data.copy(
      totalTweets = data.totalTweets + 1,
      allEmojis = updatedEmojiCounts,
      topEmojis = updateTopEntities(incoming.emojiCount, updatedEmojiCounts, data.topEmojis),
      allHashtags = updatedHashtagCounts,
      topHashtags = updateTopEntities(incoming.hashtagCount, updatedHashtagCounts, data.topHashtags),
      tweetsWithUrl = if (incoming.domainCount.nonEmpty) data.tweetsWithUrl + 1 else data.tweetsWithUrl,
      tweetsWithImageUrl = if (incoming.containsImage) data.tweetsWithImageUrl + 1 else data.tweetsWithImageUrl,
      tweetsWithEmoji = if (incoming.emojiCount.nonEmpty) data.tweetsWithEmoji + 1 else data.tweetsWithEmoji,
      allDomains =  updatedDomainCounts,
      topDomains = updateTopEntities(incoming.domainCount, updatedDomainCounts, data.topDomains)
    )
  }

  private def updateEntityCounts(incoming: Map[String, Int], previous: Map[String, Int]): Map[String, Int] = {
    incoming.foldLeft(previous) {
      case (acc, (emoji, count)) =>
        if (acc.contains(emoji)) acc + (emoji -> (acc(emoji) + count))
        else acc + (emoji -> count)
    }
  }

  private def updateTopEntities(incoming: Map[String, Int],
                                updatedCounts: Map[String, Int],
                                topCounts: Map[String, Int]): Map[String, Int] = {

    incoming.foldLeft(topCounts) {
      case (acc, (incomingEntity, incomingEntityCount)) =>
        log.debug("incoming entity [{}], incoming count [{}], acc [{}]",
          incomingEntity, incomingEntityCount, acc)

        if (acc.size < numTopEntities && !acc.contains(incomingEntity)) acc + (incomingEntity -> incomingEntityCount)
        else if (acc.contains(incomingEntity)) acc + (incomingEntity -> (acc(incomingEntity) + incomingEntityCount))
        else {
          val minEntry = acc.minBy(_._2)
          if (updatedCounts(incomingEntity) > minEntry._2) {
            (acc - minEntry._1) + (incomingEntity -> updatedCounts(incomingEntity))
          }
          else acc
        }
    }
  }

  initialize()
}
