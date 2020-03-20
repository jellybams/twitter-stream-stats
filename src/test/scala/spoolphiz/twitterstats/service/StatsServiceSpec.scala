package spoolphiz.twitterstats.service

import java.time.Instant

import com.danielasfregola.twitter4s.entities.Tweet
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class StatsServiceSpec extends AnyWordSpec with Matchers {
  "StatsService" should {
    "create map of emoji to number of occurrences in a string" in {
      val str = "An \uD83D\uDE00awesome \uD83D\uDE00string with a \uD83D\uDE00few \uD83D\uDE09emojis!"
      val tweet = Tweet(text = str, created_at = Instant.now, id = 1L, id_str = "1", source = "")
      val result = StatsService.extractEmojis(tweet)
      result.size shouldEqual 2
      result("\uD83D\uDE00") shouldEqual 3
    }
  }
}
