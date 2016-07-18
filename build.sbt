name := "AkkaActors"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4-SNAPSHOT"

libraryDependencies += "com.typesafe.play" %% "play-ws" % "2.4.3"

libraryDependencies += "com.typesafe.akka" %% "akka-persistence" % "2.4-SNAPSHOT"

// the library is available in Bintray's JCenter
resolvers += Resolver.jcenterRepo

libraryDependencies += "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.3.4"

libraryDependencies += "org.scala-lang" % "jline" % "2.11.0-M3"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"

resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"

