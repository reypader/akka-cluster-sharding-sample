package com.rmpader.gitprojects

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{Behavior, PostStop, Scheduler}
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.ShardingEnvelope
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityRef, EntityTypeKey}
import akka.http.scaladsl.server.PathMatchers._
import akka.http.scaladsl.server.PathMatcher._
import akka.http.scaladsl.Http.ServerBinding
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives.{complete, get, onSuccess, path}
import akka.actor.typed.scaladsl.AskPattern._
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import akka.persistence.typed.PersistenceId
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.util.{Failure, Success}

object Server {

  sealed trait Message

  private final case class StartFailed(cause: Throwable) extends Message

  private final case class Started(binding: ServerBinding) extends Message

  case object Stop extends Message

  def apply(host: String, port: Int): Behavior[Message] = Behaviors.setup { ctx =>
    val system = ctx.system



    val sharding = ClusterSharding(system)

    implicit val scheduler: Scheduler = ctx.system.scheduler
    implicit val untypedSystem: akka.actor.ActorSystem = ctx.system.toClassic
    implicit val ec: ExecutionContextExecutor = ctx.system.executionContext

    val TypeKey = EntityTypeKey[Counter.Command]("Counter")
    val shardRegion = sharding.init(Entity(TypeKey)(createBehavior = entityContext => Counter(entityContext.entityId, PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId))))
    implicit val timeout: Timeout = 3.seconds
    val routes = get {
      path("count" / Segment) { counterId =>
        val result = shardRegion.ask[Int](ref => ShardingEnvelope(counterId, Counter.Increment(ref)))
        onSuccess(result) { v =>
          complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, s"id:$counterId, value:$v"))
        }
      }
    }

    val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, host, port)
    ctx.pipeToSelf(serverBinding) {
      case Success(binding) => Started(binding)
      case Failure(ex) => StartFailed(ex)
    }

    def running(binding: ServerBinding): Behavior[Message] =
      Behaviors.receiveMessagePartial[Message] {
        case Stop =>
          ctx.log.info("Stopping server http://{}:{}/",
            binding.localAddress.getHostString,
            binding.localAddress.getPort)
          Behaviors.stopped
      }.receiveSignal {
        case (_, PostStop) =>
          binding.unbind()
          Behaviors.same
      }

    def starting(wasStopped: Boolean): Behaviors.Receive[Message] =
      Behaviors.receiveMessage[Message] {
        case StartFailed(cause) =>
          throw new RuntimeException("Server failed to start", cause)
        case Started(binding) =>
          ctx.log.info(
            "Server online at http://{}:{}/",
            binding.localAddress.getHostString,
            binding.localAddress.getPort)
          if (wasStopped) ctx.self ! Stop
          running(binding)
        case Stop =>
          // we got a stop message but haven't completed starting yet,
          // we cannot stop until starting has completed
          starting(wasStopped = true)
      }

    starting(wasStopped = false)
  }
}