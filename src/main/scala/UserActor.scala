package example

import example.UserActor.Messages.Commands.{Fail, SetPassword}
import UserActor.Messages.Events.{Event, PasswordChanged}
import akka.actor.{Actor, ActorLogging, Props}
import akka.persistence.PersistentActor
import example.UserMgmtActor.Messages.Commands.CreateUser

object UserMgmtActor {

  def props = Props(new UserMgmtActor)

  object Messages {

    object Events {

      sealed trait Event
      case class UserCreated(userId: String)
    }

    object Commands {

      sealed trait Command
      case object CreateUser

    }

  }

}

class UserMgmtActor extends Actor with ActorLogging {

  def receive = {
    case CreateUser => {
      val userId = java.
      context.system.actorOf(UserActor.props())
    }
  }

}

object UserActor {

  def props(userId: String) = Props(new UserActor(userId))

  object Messages {

    object Events {

      sealed trait Event
      final case class PasswordChanged(userId:String, newPassword:String) extends Event
    }

    object Commands {

      sealed trait Command
      final case class SetPassword(password: String) extends Command
      case object Fail extends Command

    }

  }

}

class UserActor(userId: String) extends PersistentActor with ActorLogging {

  var password: String = null

  override def persistenceId: String = userId

  override def preStart = {
    log.info(s"created user actor for user with id $userId")
  }

  def updateState(event: Event) = {
    event match {
      case PasswordChanged(someUserId, newPassword) if someUserId == userId => {
        password = newPassword
      }
    }
  }

  override def receiveRecover: Receive = {
    case evt: Event =>
      updateState(evt)
  }



  override def receiveCommand: Receive = {

    case SetPassword(newPassword: String) =>
      val event = PasswordChanged(userId, newPassword)
      persist(event)(updateState)
      context.system.eventStream.publish(event)

    case Fail =>
      throw new Exception()

    case msg =>
      log.info("don't understand this message")
  }


}
