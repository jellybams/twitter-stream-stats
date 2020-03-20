import com.typesafe.config.ConfigFactory

name := "twitter-stream-stats"

version := "0.1"

scalaVersion := "2.12.4"

libraryDependencies += "com.typesafe" % "config" % "1.3.4"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.0"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"

libraryDependencies += "com.danielasfregola" %% "twitter4s" % "6.1"

libraryDependencies += "com.vdurmont" % "emoji-java"  % "5.1.1"

libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.5.22" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.22"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"

testOptions in Test += Tests.Setup( () => {
  System.setProperty("config.resource", "application.test.conf")
  ConfigFactory.invalidateCaches()
}
)