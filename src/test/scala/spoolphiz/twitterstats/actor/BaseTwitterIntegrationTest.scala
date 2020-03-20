package spoolphiz.twitterstats.actor

import java.time.Instant

import akka.actor.ActorSystem
import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit}
import com.danielasfregola.twitter4s.entities.{Entities, HashTag, Tweet, UrlDetails}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike

class BaseTwitterIntegrationTest extends TestKit(ActorSystem("TestSystem"))
  with ActorKiller
  with DefaultTimeout
  with ImplicitSender
  with AnyWordSpecLike
  with Matchers
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with ScalaFutures {



  def splitEntityPool(countPerGroup: Int,
                      totalDesiredGroups: Int,
                      entityPool: Map[String, Int]): List[List[String]] = {

    val entityList = entityPool.flatMap {
      case (entity, count) => List.fill(count)(entity)
    }.toList

    if (entityList.isEmpty) List.fill(totalDesiredGroups)(List.empty)
    else {
      val grouped = entityList.grouped(countPerGroup).toList
      grouped.padTo(totalDesiredGroups, List.empty)
    }
  }

  def generateTweets(count: Int,
                     emojis: Map[String, Int] = Map.empty,
                     hashtags: Map[String, Int] = Map.empty,
                     urls: Map[String, Int] = Map.empty,
                     imageUrls: Map[String, Int] = Map.empty): Seq[Tweet] = {

    val emojisPerTweet = math.ceil(emojis.values.sum.toDouble / count.toDouble).toInt
    val hashtagsPerTweet = math.ceil(hashtags.values.sum.toDouble / count.toDouble).toInt
    val urlsPerTweet = math.ceil(urls.values.sum.toDouble / count.toDouble).toInt
    val imageUrlsPerTweet = math.ceil(imageUrls.values.sum.toDouble / count.toDouble).toInt

    val emojiPool = splitEntityPool(emojisPerTweet, count, emojis)
    val hashtagPool = splitEntityPool(hashtagsPerTweet, count, hashtags)

    doGenTweets(Seq.empty, count, emojiPool, hashtagPool)
  }

  def assembleTweetEntities(rawHashtags: Option[List[String]],
                            rawUrls: Option[List[String]],
                            rawImageUrls: Option[List[String]]): Option[Entities] = {
    val hashtags = rawHashtags match {
      case Some(tagList) => tagList.map(HashTag(_, Seq.empty))
      case None => Seq.empty
    }

    val urls = rawUrls match {
      case Some(urlList) => urlList.map(item => UrlDetails(item, item, item, Seq.empty))
      case None => Seq.empty
    }

    Some(Entities(
      hashtags = hashtags,
      urls = urls
    ))
  }

  def doGenTweets(acc: Seq[Tweet],
                  count: Int,
                  emojiPool: List[List[String]] = List.empty,
                  hashtagPool: List[List[String]] = List.empty,
                  urlPool: List[List[String]] = List.empty,
                  imageUrlPool: List[List[String]] = List.empty): Seq[Tweet] = {

    if (count == 0) acc
    else {
      val emojiStr = emojiPool.headOption match {
        case Some(emojiList) => emojiList.mkString(" ")
        case None => ""
      }

      val entities = assembleTweetEntities(hashtagPool.headOption, urlPool.headOption, imageUrlPool.headOption)

      val updatedAcc = acc :+ Tweet(
        contributors = Seq(),
        coordinates = None,
        created_at = Instant.now(),
        id = count,
        id_str = count.toString,
        source = "",
        text = emojiStr + "tweet text",
        current_user_retweet = None,
        entities = entities,
        extended_entities = None,
        extended_tweet = None,
        favorite_count = 0,
        favorited = false,
        filter_level = None,
        geo = None,
        in_reply_to_screen_name = None,
        in_reply_to_status_id = None,
        in_reply_to_user_id_str = None,
        in_reply_to_status_id_str = None,
        in_reply_to_user_id = None,
        is_quote_status = false,
        lang = None,
        place = None,
        possibly_sensitive = false,
        quoted_status_id = None,
        quoted_status_id_str = None,
        quoted_status = None,
        scopes = Map.empty,
        retweet_count = 0,
        retweeted = false,
        retweeted_status = None,
        truncated = false,
        display_text_range = None,
        user = None,
        withheld_copyright = false,
        withheld_in_countries = Seq.empty,
        withheld_scope = None,
        metadata = None
      )

      doGenTweets(
        updatedAcc,
        count - 1,
        if (emojiPool.nonEmpty) emojiPool.tail else List.empty,
        if (hashtagPool.nonEmpty) hashtagPool.tail else List.empty,
        if (urlPool.nonEmpty) urlPool.tail else List.empty,
        if (imageUrlPool.nonEmpty) imageUrlPool.tail else List.empty
      )
    }
  }
}
