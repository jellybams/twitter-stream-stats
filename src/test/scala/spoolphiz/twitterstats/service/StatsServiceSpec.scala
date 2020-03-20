package spoolphiz.twitterstats.service

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class StatsServiceSpec extends AnyWordSpec with Matchers {
  "StatsService" should {
    "create map of emoji to number of occurrences in a string" in {
      val str = "An \uD83D\uDE00awesome \uD83D\uDE00string with a \uD83D\uDE00few \uD83D\uDE09emojis!"
      val result = StatsService.extractEmojis(str)
      result.size shouldEqual 2
      result("\uD83D\uDE00") shouldEqual 3
    }
  }
}
