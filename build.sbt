import com.typesafe.config.ConfigFactory

name := "twitter-stream-stats"

version := "0.1"

scalaVersion := "2.12.4"

val deps = Seq(
  "com.typesafe" % "config" % "1.3.4",
  "org.scalactic" %% "scalactic" % "3.1.0",
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "com.danielasfregola" %% "twitter4s" % "6.1",
  "com.vdurmont" % "emoji-java" % "5.1.1",
  "com.typesafe.akka" %% "akka-testkit" % "2.5.22" % Test,
  "com.typesafe.akka" %% "akka-actor" % "2.5.22",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

libraryDependencies ++= deps

testOptions in Test += Tests.Setup(() => {
  System.setProperty("config.resource", "application.test.conf")
  ConfigFactory.invalidateCaches()
})
