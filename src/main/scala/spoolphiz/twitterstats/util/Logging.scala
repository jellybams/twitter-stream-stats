package spoolphiz.twitterstats.util

import org.slf4j.{Logger, LoggerFactory}


trait Logging {
  val loggerName: String = this.getClass.getName
  lazy val log: Logger = LoggerFactory.getLogger(loggerName)
}
