package spoolphiz.twitterstats.actor.stats

import akka.actor.{ActorRef, LoggingFSM, OneForOneStrategy, Props, SupervisorStrategy}
import akka.routing.RoundRobinPool
import com.danielasfregola.twitter4s.entities.Tweet
import spoolphiz.twitterstats.actor.stats.StatsProcessorSupervisor.{ActorData, ActorState, Ready, ReadyData}
import spoolphiz.twitterstats.util.ConfigModule

object StatsProcessorSupervisor {
  def props(statsCollector: ActorRef) = Props(new StatsProcessorSupervisor(statsCollector))

  sealed trait ActorState

  sealed trait ActorData

  case object Ready extends ActorState

  case class ReadyData(workerRouter: ActorRef) extends ActorData
}

class StatsProcessorSupervisor(statsCollector: ActorRef) extends LoggingFSM[ActorState, ActorData] {

  private val numWorkers: Int = ConfigModule.app.getInt("actors.statsProcessorSupervisor.numWorkers")

  startWith(Ready, ReadyData(workerRouter = startWorkerPool()))

  when(Ready) {
    case Event(t: Tweet, sd: ReadyData) => sendTweetToRouter(t, sd)
    case Event(_, _) => stay
  }

  private def sendTweetToRouter(tweet: Tweet, stateData: ReadyData) = {
    stateData.workerRouter ! tweet
    stay
  }

  private def startWorkerPool() = {
    log.info("starting StatsProcessorSupervisor worker pool with {} workers", numWorkers)

    val routerPoolSupervisionStrategy: OneForOneStrategy = OneForOneStrategy() {
      case e => SupervisorStrategy.restart
    }

    val routerProps = RoundRobinPool(numWorkers, None, routerPoolSupervisionStrategy)
      .props(StatsProcessorActor.props(statsCollector))

    context.actorOf(routerProps, "statsProcessorRouter")
  }

  initialize()
}
