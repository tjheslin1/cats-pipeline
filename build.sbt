import Dependencies._

lazy val root = (project in file(".")).settings(libraryDependencies ++= Seq(
  akkaActor, akkaStream, akkaHttp, scalatest, circeCore, circeGeneric, circeParser
))

scalacOptions += "-Ypartial-unification"