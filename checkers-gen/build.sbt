import Dependencies._

ThisBuild / scalaVersion     := "2.12.10"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "checkers",
    libraryDependencies ++= List(
      scalaTest % Test,
      "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-beta5",
      "org.nd4j" % "nd4j-cuda-10.0-platform" % "1.0.0-beta5"
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
