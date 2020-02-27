package com.rmpader.gitprojects

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object Counter {

  case class Request(replyTo: ActorRef[Response])

  case class Response(value: Int)

  def apply(): Behavior[Request] = Behaviors.setup { context =>
    increment(0)
  }

  def increment(value: Int): Behavior[Request] = Behaviors.receiveMessage { message =>
    val newVal = value + 1
    message.replyTo ! Response(newVal)
    increment(newVal)
  }

}
