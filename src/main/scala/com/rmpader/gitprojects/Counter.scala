package com.rmpader.gitprojects

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}

object Counter {

  sealed trait Command extends JsonSerializable

  case class Increment(replyTo: ActorRef[Int]) extends Command

  sealed trait Event extends JsonSerializable

  case class Incremented(value: Int) extends Event

  case class State(value: Int)


  def commandHandler(entityId:String, context: ActorContext[Command]): (State, Command) => Effect[Event, State] = { (state, command) =>
    command match {
      case Increment(replyTo) =>
        val newVal = state.value + 1
        context.log.info(s"Increment Counter $entityId to $newVal")
        Effect.persist(Incremented(newVal)).thenReply(replyTo) { _ => newVal }
    }
  }
  def eventHandler(entityId:String, context: ActorContext[Command]): (State, Event) => State = { (state, event) =>
    event match {
      case Incremented(newVal) =>
        context.log.info(s"Incremented Counter $entityId to $newVal")
        state.copy(value = newVal)
    }
  }

  def apply(entityId: String, persistenceId: PersistenceId): Behavior[Command] = Behaviors.setup { context =>
    EventSourcedBehavior[Command, Event, State](
      persistenceId = persistenceId,
      emptyState = State(0),
      commandHandler = commandHandler(entityId, context),
      eventHandler = eventHandler(entityId, context))
  }

}
