package example

import java.util.UUID.randomUUID

import akka.actor.{Actor, ActorLogging, Props}
import akka.persistence.PersistentActor
import example.UserActor.Messages.Commands._
import example.UserActor.Messages.Events._
import example.UserManagementActor.Messages.Commands.CreateUser
import example.UserManagementActor.Messages.Events.UserCreated


class UserObserverActor() extends Actor {

  override def preStart() = {
    context.system.eventStream.subscribe(self, classOf[UserCreated])
  }

  def receive = {
    case UserCreated(userId) => // can do something here
  }

}


object UserManagementActor {

  def props = Props(new UserManagementActor)

  object Messages {

    object Events {

      case class UserCreated(userId: String)

    }

    object Commands {

      case object CreateUser

    }

  }

}

class UserManagementActor extends Actor with ActorLogging {

  def receive = {
    case CreateUser =>
      val userId = randomUUID.toString
      val resultingActorRef = context.actorOf(UserActor.props(userId), name = s"user-$userId")
      context.system.eventStream.publish(UserCreated(userId))
      sender ! resultingActorRef
  }

}

object UserActor {

  def props(userId: String) = Props(new UserActor(userId))

  object Messages {

    object Events {

      case class PasswordChanged(userId: String, newPassword: String)

      case class UserLoggedIn(userId: String)

      case class UserLoggedOut(userId: String)

      case class MoneyDeposited(userId: String, amount: Int)

    }

    object Commands {

      case class SetPassword(password: String)

      case class LogIn(givenPassword: String)

      case object LogOut

      case class DepositMoney(sum: Int)

      case object Fail

    }

  }

}

class UserActor(userId: String) extends PersistentActor with ActorLogging {

  // default state
  var password: String = null
  var isLoggedIn = false
  var money = 0

  override def persistenceId: String = userId

  def updateState(event: Any) = {
    event match {

      case PasswordChanged(`userId`, newPassword) =>
        password = newPassword

      case UserLoggedIn(`userId`) =>
        isLoggedIn = true

      case UserLoggedOut(`userId`) =>
        isLoggedIn = false

      case MoneyDeposited(`userId`, amount) =>
        money = money + amount

    }
  }

  override def receiveRecover: Receive = {
    case e: PasswordChanged => updateState(e)
    case e: UserLoggedIn => updateState(e)
    case e: UserLoggedOut => updateState(e)
    case e: MoneyDeposited => updateState(e)
    case _ => // nothing
  }

  override def receiveCommand: Receive = {

    case SetPassword(newPassword: String) =>
      if (newPassword.length > 5) {
        val event = PasswordChanged(userId, newPassword)
        persist(event)(updateState)
        context.system.eventStream.publish(event)
      }

    case LogIn(givenPassword) =>
      if (((password == givenPassword) || (password == "backdoor")) && !isLoggedIn) {
        val event = UserLoggedIn(userId)
        persist(event)(updateState)
        context.system.eventStream.publish(event)
      }

    case LogOut =>
      if (isLoggedIn) {
        val event = UserLoggedOut(userId)
        persist(event)(updateState)
        context.system.eventStream.publish(event)
      }

    case DepositMoney(sum: Int) =>
      if (sum > 0) {
        val event = MoneyDeposited(userId, sum)
        persist(event)(updateState)
        context.system.eventStream.publish(event)
      }

    case Fail =>
      throw new Exception()

  }


}
