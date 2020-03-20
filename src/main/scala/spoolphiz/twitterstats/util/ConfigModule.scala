package spoolphiz.twitterstats.util

import com.typesafe.config.{Config, ConfigFactory}

object ConfigModule {
  val all: Config = ConfigFactory.load()
  val app: Config = all.getConfig("app")
}
