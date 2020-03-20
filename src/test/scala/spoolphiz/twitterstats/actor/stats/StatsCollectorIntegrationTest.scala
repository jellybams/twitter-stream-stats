package spoolphiz.twitterstats.actor.stats

import akka.actor.ActorRef
import akka.pattern.ask
import com.danielasfregola.twitter4s.entities.Tweet
import spoolphiz.twitterstats.actor.BaseTwitterIntegrationTest
import spoolphiz.twitterstats.actor.stats.StatsCollectorActor.GetStats
import spoolphiz.twitterstats.domain.StatsData

import scala.concurrent.ExecutionContext


class StatsCollectorIntegrationTest extends BaseTwitterIntegrationTest {

  var statsCollectorActor: ActorRef = _
  var statsProcessorSupervisor: ActorRef = _

  implicit def executionContext: ExecutionContext = system.dispatcher

  override def beforeAll(): Unit = {
    startActors()
  }

  override def afterEach(): Unit = {
    terminateActor(statsCollectorActor)
    terminateActor(statsProcessorSupervisor)
    startActors()
  }

  override def afterAll: Unit = {
    shutdown()
  }

  def startActors(): Unit = {
    statsCollectorActor = system.actorOf(
      StatsCollectorActor.props(),
      "stats-collector-actor"
    )

    statsProcessorSupervisor = system.actorOf(
      StatsProcessorSupervisor.props(statsCollectorActor),
      "stats-processor-supervisor"
    )
  }

  "StatsCollector" should {
    "count total tweets received" in {
      val numTweets = 10
      val tweets: Seq[Tweet] = generateTweets(numTweets)
      tweets.foreach(statsProcessorSupervisor ! _)

      Thread.sleep(500)

      val resultF = (statsCollectorActor ? GetStats).mapTo[StatsData]

      whenReady(resultF) { result => result.totalTweets shouldEqual numTweets }
    }

    "track top emojis" in {
      val emojis = Map(
        "\uD83D\uDE00" -> 10,
        "\uD83D\uDC4C" -> 3,
        "\uD83D\uDE09" -> 4,
        "\uD83D\uDC4F" -> 14
      )

      val tweets = generateTweets(9, emojis)

      tweets.foreach(statsProcessorSupervisor ! _)

      Thread.sleep(500)

      val resultF = (statsCollectorActor ? GetStats).mapTo[StatsData]

      whenReady(resultF) { result =>
        result.topEmojis.size shouldEqual 3
        result.topEmojis("\uD83D\uDE00") shouldEqual emojis("\uD83D\uDE00")
        result.topEmojis("\uD83D\uDE09") shouldEqual emojis("\uD83D\uDE09")
        result.topEmojis("\uD83D\uDC4F") shouldEqual emojis("\uD83D\uDC4F")
        result.tweetsWithEmoji should be > 0.0
      }
    }

    "track top hashtags" in {
      val hashtags = Map(
        "foo" -> 3,
        "bar" -> 4,
        "baz" -> 1,
        "beep" -> 6
      )

      val tweets = generateTweets(count = 12, hashtags = hashtags)

      tweets.foreach(statsProcessorSupervisor ! _)

      Thread.sleep(500)

      val resultF = (statsCollectorActor ? GetStats).mapTo[StatsData]

      whenReady(resultF) { result =>
        result.topHashtags.size shouldEqual 3
        result.topHashtags("beep") shouldEqual hashtags("beep")
        result.topHashtags("bar") shouldEqual hashtags("bar")
        result.topHashtags("foo") shouldEqual hashtags("foo")
      }
    }

    "track top domains" in {
      val domains = Map(

      )
    }
  }
}
