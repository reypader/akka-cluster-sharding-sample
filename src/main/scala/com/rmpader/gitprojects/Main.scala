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

object Main extends App {
  val configuration: Config = ConfigFactory.load()
  val envConfiguration: Config = ConfigFactory.load(configuration.getString("app.config-env"))
    .withFallback(configuration)
  val host = envConfiguration.getString("app.host")
  val port = envConfiguration.getInt("app.port")
  val name = envConfiguration.getString("app.name")
  val system: ActorSystem[Server.Message] = ActorSystem(Server(host, port), name, envConfiguration)
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

