package spoolphiz.twitterstats.domain

case class StatsData(
                      runTimeSecs: Long = 0,
                      totalTweets: Int = 0,
                      tweetsWithEmoji: Double = 0.0,
                      tweetsWithEmojiPercent: Double = 0,
                      topEmojis: Map[String, Int] = Map.empty,
                      topHashtags: Map[String, Int] = Map.empty,
                      topDomains: Map[String, Int] = Map.empty,
                      tweetsWithUrlPercent: Double = 0.0,
                      tweetsWithImageUrlPercent: Double = 0.0,
                      averageTweets: TweetCountAverages = TweetCountAverages()
                    )
