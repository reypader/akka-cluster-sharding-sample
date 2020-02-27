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
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.concurrent.Future

object App extends App {

  implicit val system: ActorSystem[Server.Message] = ActorSystem(Server("localhost", 8080), "BuildJobsServer")
  val configuration: Config = ConfigFactory.load()
  val cs: CoordinatedShutdown = CoordinatedShutdown(system)
  AkkaManagement(system).start()
  cs.addTask(CoordinatedShutdown.PhaseServiceUnbind, "stop-HTTP-server") {
    () => {
      system ! Server.Stop
      Future.successful(Done)
    }
  }
}

