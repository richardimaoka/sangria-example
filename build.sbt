import Dependencies._

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"
val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"
lazy val root = (project in file("."))
  .settings(
    name := "sangria-example",
    libraryDependencies ++= Seq(
      "org.sangria-graphql" %% "sangria" % "2.0.0",
      "org.sangria-graphql" %% "sangria-circe" % "1.3.1",
      "io.circe" %% "circe-generic" % "0.13.0",
      "org.sangria-graphql" %% "sangria-spray-json" % "1.0.2",
      "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
      "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
      "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
        scalaTest % Test
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
