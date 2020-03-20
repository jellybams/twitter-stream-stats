package spoolphiz.twitterstats.actor.stats

import akka.actor.{ActorRef, LoggingFSM, Props}
import akka.pattern.pipe
import com.danielasfregola.twitter4s.TwitterStreamingClient
import com.danielasfregola.twitter4s.entities.{AccessToken, ConsumerToken}
import com.danielasfregola.twitter4s.http.clients.streaming.{ErrorHandler, TwitterStream}
import spoolphiz.twitterstats.actor.stats.TwitterStreamSupervisor._
import spoolphiz.twitterstats.util.ConfigModule

import scala.concurrent.Future

object TwitterStreamSupervisor {
  def props(statsProcessor: ActorRef) = Props(new TwitterStreamSupervisor(statsProcessor))

  sealed trait ActorState

  sealed trait ActorData

  case object Starting extends ActorState

  case class StartingData() extends ActorData

  case object Running extends ActorState

  case class RunningData(stream: TwitterStream) extends ActorData

}

class TwitterStreamSupervisor(statsProcessor: ActorRef) extends LoggingFSM[ActorState, ActorData] {

  import context.dispatcher

  val consumerToken = ConsumerToken(
    key = ConfigModule.app.getString("twitter.auth.consumerKey"),
    secret = ConfigModule.app.getString("twitter.auth.consumerSecret")
  )

  val accessToken = AccessToken(
    key = ConfigModule.app.getString("twitter.auth.accessToken"),
    secret = ConfigModule.app.getString("twitter.auth.accessTokenSecret")
  )

  val streamingClient = TwitterStreamingClient(consumerToken, accessToken)


  startWith(Starting, StartingData())

  when(Starting) {
    case Event(stream: TwitterStream, _: StartingData) => goto(Running) using RunningData(stream)
  }

  /**
    * this is where error handling would go
    */
  when(Running) {
    case _ =>
      log.warning("no behavior configured for `Running` state")
      stay
  }

  override def preStart(): Unit = {
    super.preStart()

    startStatusStream pipeTo self
  }

  private def startStatusStream: Future[TwitterStream] = streamingClient.sampleStatuses()({
    case msg =>
      statsProcessor ! msg
  }, {
    case _ => ErrorHandler.ignore // TODO don't ignore errors, determine if stream should be restarted
  })

  initialize()
}
