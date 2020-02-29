package com.rmpader.gitprojects

import akka.{Done, actor => classic}
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.management.scaladsl.AkkaManagement
import com.typesafe.config.{Config, ConfigFactory}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.Cluster
import akka.http.scaladsl.server.Route
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.stream.ActorMaterializer

import scala.concurrent.Future

class Main(nr: Int) {
  val configuration: Config = ConfigFactory.parseString(
    s"""
      akka.remote.artery.canonical.hostname = "127.0.0.$nr"
      akka.management.http.hostname = "127.0.0.$nr"
    """).withFallback(ConfigFactory.load())
  val system: ActorSystem[Server.Message] = ActorSystem(Server("localhost", 8080+nr), "ClusterProcessor", configuration)
  AkkaManagement(system).start()
  ClusterBootstrap(system).start()
  val cs: CoordinatedShutdown = CoordinatedShutdown(system)
  cs.addTask(CoordinatedShutdown.PhaseServiceUnbind, "stop-HTTP-server") {
    () => {
      system ! Server.Stop
      Future.successful(Done)
    }
  }

}

