package example

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout
import example.UserActor.Messages.Commands.SetPassword
import org.slf4j.LoggerFactory.getLogger

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.tools.jline.console.ConsoleReader
import scala.util.{Failure, Random, Success}

class Main

object Main extends App {

  val logger = getLogger(classOf[Main])

  val system = ActorSystem("system")

  val userManagementActor = system.actorOf(Props(new UserManagementActor), "user-group")

  // CLI system

  val consoleReader = new ConsoleReader()
  consoleReader.setPrompt("> ")

  val observerActor = system.actorOf(Props(new UserObserverActor()), "observer")


  val CreateUser = "create user"
  val SetUserPassword = "set user password ([a-zA-Z0-9]*) ([a-zA-Z0-9]*)".r
  val FailUser = "fail ([a-zA-Z0-9]*)".r
  val HowManyLoggedIn = "num users logged in"
  val Populate = "populate"

  implicit val timeout = Timeout(5 seconds) // for various actor selections
  import system.dispatcher

  while (true) {

    val line: String = consoleReader.readLine()

    line match {

      case CreateUser =>
        userManagementActor ! UserManagementActor.Messages.Commands.CreateUser

      case SetUserPassword(userId, newPassword) =>
          system.actorSelection(s"/user/user-group/user-$userId").resolveOne().onComplete {
            case Success(someUserActor: ActorRef) =>
              someUserActor ! SetPassword(newPassword)
            case Failure(_) =>
              logger.info(s"failed to find user with id $userId")
          }

      case FailUser(userId) =>
          system.actorSelection(s"/user/user-group/user-$userId").resolveOne().onComplete {
            case Success(someUserActor: ActorRef) =>
              someUserActor ! UserActor.Messages.Commands.Fail
            case Failure(_) =>
              logger.info(s"failed to find user with id $userId")
          }

      case Populate =>

        def randomCommand: Any = {
          import UserActor.Messages.Commands
          val commands = Set[Any] (
            Commands.SetPassword(Random.alphanumeric.take(8).mkString),
            Commands.DepositMoney(Random.nextInt(100)),
            Commands.LogIn(Random.nextBoolean() match {
              case true => "backdoor"
              case false => Random.alphanumeric.take(8).mkString
            }),
            Commands.LogOut)
          commands.maxBy(_ => scala.util.Random.nextInt())
        }

        var i = 0
        while (i < 10000) {
          (userManagementActor ? UserManagementActor.Messages.Commands.CreateUser).mapTo[ActorRef].onComplete {
            case Success(actorRef) =>
              (1 to 1000).foreach { _ =>
                actorRef ! randomCommand
              }
            case Failure(_) => // do nothing
          }
            i = i + 1
        }
        logger.info("done creating user actors")

        logger.info("done sending commands")


      case _ => logger.info("command not recognized")

    }

  }

}



