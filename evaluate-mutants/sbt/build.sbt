name := "bus"

organization := "org.ru.throstur"

version := "0.1"

scalaVersion := "2.11.5" // Scala is a dependency (!)

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.9"
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.3.9" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % Test

