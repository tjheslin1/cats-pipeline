import sbt._

object Dependencies {

  val akkaVersion = "2.5.7"
  val akkaHttpVersion = "10.0.11"
  val catsVersion = "1.0.0-RC1"
  val circeVersion = "0.8.0"

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  val circeCore = "io.circe" %% "circe-core" % circeVersion
  val circeGeneric = "io.circe" %% "circe-generic" % circeVersion
  val circeParser = "io.circe" %% "circe-parser" % circeVersion

  val cats = "org.typelevel" %% "cats-core" % catsVersion

  val scalatest = "org.scalatest" %% "scalatest" % "3.0.1" % Test
}
