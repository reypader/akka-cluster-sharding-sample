scalaVersion := "2.13.1"

name := "sync-async-sync-processing"
organization := "com.rmpader.gitprojects"
version := "1.0"
description :="""
    Project to demonstrate bridging synchronous requests from HTTP, down to SQS and then back to a different instance
    then back to the original instance and then response.

    Uses Akka Cluster Sharding on Kubernetes Clusters.

    HTTP ⇄  Instance A  ─┐
                 ↑      SQS
            Instance B <─┘
    """

libraryDependencies += "com.lightbend.akka.management" %% "akka-management" % "1.0.5"
libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.6.3"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.1.11"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.11"
libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.6.3"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies += "com.typesafe" % "config" % "1.4.0"

fork in run := true

enablePlugins(AshScriptPlugin)
enablePlugins(JavaServerAppPackaging)

mainClass in Compile := Some("com.rmpader.gitprojects.App")
discoveredMainClasses in Compile := Seq()
dockerBaseImage := "openjdk:8-alpine"
dockerExposedPorts := Seq(8558, 8080)
