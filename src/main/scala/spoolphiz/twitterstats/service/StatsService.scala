package spoolphiz.twitterstats.service

import java.net.URL

import com.danielasfregola.twitter4s.entities.{Entities, HashTag, Tweet}
import com.vdurmont.emoji.EmojiParser
import spoolphiz.twitterstats.domain.ParsedDomain
import spoolphiz.twitterstats.util.{ConfigModule, Logging}

import collection.JavaConverters._

object StatsService extends Logging {

  val imgDomains = ConfigModule.app.getStringList("twitter.imageDomains").asScala.toList

  def extractEmojis(text: String): Map[String, Int] = {
    val extracted = EmojiParser.extractEmojis(text).asScala

    val grouped = extracted.foldLeft(Map.empty[String, Int]) {
      case (acc, emoji) =>
        if (acc.contains(emoji)) acc + (emoji -> (acc(emoji) + 1))
        else acc + (emoji -> 1)
    }

    log.debug(s"parsed text [$text], into groups [$grouped]")

    grouped
  }

  def extractHashtags(entities: Option[Entities]): Map[String, Int] = {
    entities match {
      case Some(ents) =>
        ents.hashtags.foldLeft(Map.empty[String, Int]) {
          case (acc, hashtag: HashTag) =>
            if (acc.contains(hashtag.text)) acc + (hashtag.text -> (acc(hashtag.text) + 1))
            else acc + (hashtag.text -> 1)
        }
      case None => Map.empty
    }
  }

  /**
    * entity extraction is only done for the top level status,
    * any additional entities from retweets and quotes are ignored
    *
    * if a tweet is truncated use the extended_tweet object instead, see the following
    * https://developer.twitter.com/en/docs/tweets/data-dictionary/overview/intro-to-tweet-json#extendedtweet
    * https://developer.twitter.com/en/docs/tweets/data-dictionary/overview/tweet-object
    *
    * @param tweet
    * @return
    */
  def extractDomainCounts(tweet: Tweet): Map[ParsedDomain, Int] = {
    if (tweet.truncated && tweet.extended_tweet.nonEmpty)
      mergeEntityCounts(
        extractDomains(tweet.extended_tweet.get.entities),
        extractDomains(tweet.extended_tweet.get.extended_entities)
      )
    else
      mergeEntityCounts(
        extractDomains(tweet.entities),
        extractDomains(tweet.extended_entities)
      )
  }


  def mergeEntityCounts[T](first: Map[T, Int], second: Map[T, Int]): Map[T, Int] = {
    first ++ second.map { case (k, v) => k -> (v + first.getOrElse(k, 0)) }
  }

  def extractDomains(entities: Option[Entities]): Map[ParsedDomain, Int] = {
    val entityList = entities match {
      case Some(ents) => {
        val firstCollection = ents.url match {
          case Some(exists) =>
            exists.urls.map { urlDetail =>
              val parsed = new URL(urlDetail.expanded_url)
              ParsedDomain(domain = parsed.getHost, isImage = isImage(parsed))
            }
          case None => Seq.empty
        }

        val secondCollection = ents.urls.map { urlDetail =>
          val parsed = new URL(urlDetail.expanded_url)
          ParsedDomain(domain = parsed.getHost, isImage = isImage(parsed))
        }

        val fromMediaCollection = ents.media.map { media =>
          val parsed = new URL(media.expanded_url)
          ParsedDomain(domain = parsed.getHost, isImage = isImage(parsed), fromMedia = true)
        }

        firstCollection ++ secondCollection ++ fromMediaCollection
      }

      case None => Seq.empty
    }

    entityList.foldLeft(Map.empty[ParsedDomain, Int]){
      case (acc, parsedDomain) =>
        if (acc.contains(parsedDomain)) acc + (parsedDomain -> (acc(parsedDomain) + 1))
        else acc + (parsedDomain -> 1)
    }
  }

  def isImage(url: URL): Boolean = {
    imgDomains.contains(url.getHost)
  }
}
