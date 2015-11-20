name := """thinkit-reactive-sample"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  "com.typesafe.slick" %% "slick" % "3.1.0",
  "com.typesafe.play" %% "play-slick" % "1.1.1",
  "com.h2database" % "h2" % "1.3.176",
  "com.zaxxer" % "HikariCP" % "2.3.7",
  cache,
  ws,
  specs2 % Test
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


