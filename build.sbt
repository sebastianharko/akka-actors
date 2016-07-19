name := "AkkaActors"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT"

libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.4.3"

libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.4-SNAPSHOT"

libraryDependencies += "com.github.krasserm" %% "akka-persistence-cassandra3" % "0.5"

libraryDependencies += "org.scala-lang" % "jline" % "2.11.0-M3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

resolvers += "krasserm at bintray" at "http://dl.bintray.com/krasserm/maven"

