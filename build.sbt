import com.typesafe.sbt.packager.Keys._
import com.typesafe.sbt.packager.universal.Keys.{normalizedName, version}

name := """gateway2"""

version := "1.3-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala, SbtWeb)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.rapplogic" % "xbee-api" % "0.9.1",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.6", // for XBEE Logging
  //"org.rxtx" % "rxtxcomm" % "2007-xbee-0.9",
  "xalan" % "serializer" % "2.7.2", //fixes resovler bug in IDEA14 http://stackoverflow.com/questions/26633298/sbt-xalanserializer-error-in-intellij
  "net.sigusr" %% "scala-mqtt-client" % "0.6.0",
  "com.github.jodersky" %% "flow" % "2.2.0",
  "com.github.nscala-time" %% "nscala-time" % "2.0.0"
  //"mysql" % "mysql-connector-java" % "5.1.6"
)

libraryDependencies ++= Seq(
  "org.webjars" %% "webjars-play" % "2.3.0",
  "org.webjars" % "highcharts" % "4.0.3",
  "org.webjars" % "bootstrap" % "3.2.0",
  "org.webjars" % "openlayers" % "2.13.1",
  "org.webjars" % "jquery" % "2.1.1"
)

includeFilter in (Assets, LessKeys.less) := "*.less"

excludeFilter in (Assets, LessKeys.less) := "_*.less"

//resolvers += "smart releases" at "http://dev.smart-project.info/artifactory/libs-release/"

//resolvers += "smart snapshots" at "http://dev.smart-project.info/artifactory/libs-snapshot/"

resolvers += "maven central" at "http://central.maven.org/maven2/"

resolvers += "spring releases" at "http://repo.spring.io/libs-release-remote/"

scalacOptions in ThisBuild ++= Seq(
  "-encoding", "UTF-8",
  "-deprecation", // warning and location for usages of deprecated APIs
  "-feature", // warning and location for usages of features that should be imported explicitly
  "-unchecked", // additional warnings where generated code depends on assumptions
  "-Xlint", // recommended additional warnings
  "-Ywarn-adapted-args", // Warn if an argument list is modified to match the receiver
  "-Ywarn-value-discard", // Warn when non-Unit expression results are unused
  "-Ywarn-inaccessible",
  "-Ywarn-dead-code",
  "-language:reflectiveCalls"
)

javacOptions in Compile ++= Seq(
  "-encoding", "UTF-8",
  "-g",
  "-Xlint:-path",
  "-Xlint:deprecation",
  "-Xlint:unchecked"
)

//slightly modified version of https://stackoverflow.com/questions/30069329/add-timestamp-to-zip-created-by-sbt-native-packager

name in Universal := {
  val name = normalizedName.value + "-" + version.value
  def timestamp = new java.text.SimpleDateFormat("yyyyMMdd") format new java.util.Date()
  if (isSnapshot.value) s"$name-$timestamp" else name
}
