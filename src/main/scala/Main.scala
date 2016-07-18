package example

import akka.actor._
import akka.pattern.ask
import example.UserActor.Messages.Commands.SetPassword
import example.UserActor.Messages.Events.PasswordChanged
import play.api.libs.json.{JsLookupResult, JsNumber}
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.tools.jline.console.ConsoleReader
import scala.util.{Success, Failure}

object CurrencyExchangeActor {

  def props = Props(new CurrencyExchangeActor)

  object Messages {

    case class ExchangeRateRequest(currency: String)

  }

}


class CurrencyExchangeActor extends Actor with ActorLogging {

  import CurrencyExchangeActor.Messages._
  import akka.pattern.pipe
  import context.dispatcher
  import wsClient.url

  val wsClient = NingWSClient()

  def receive = {
    case ExchangeRateRequest(currency) =>
      pipe {
        val result: Future[JsLookupResult] = url(s"http://api.fixer.io/latest?symbols=$currency").get()
          .map(_.json \ "rates" \ currency)
        result
      }.to(sender)
      
  }

}

class UserObserverActor extends Actor with ActorLogging {

  override def preStart = context.system.eventStream.subscribe(self, classOf[PasswordChanged])

  def receive = {
    case e: PasswordChanged =>
      log.info("password changed for user with id " + e.userId + " to " + e.newPassword)
  }

}



class Main

object Main extends App {

  val logger = org.slf4j.LoggerFactory.getLogger(classOf[Main])

  val system = ActorSystem("system")
  val actor = system.actorOf(CurrencyExchangeActor.props)

  implicit val timeout = akka.util.Timeout(5 seconds)
  import system.dispatcher
  (actor ? CurrencyExchangeActor.Messages.ExchangeRateRequest("CAD")).mapTo[JsLookupResult].foreach {
    result =>
      result.toOption match {
        case Some(v: JsNumber) => println(v.value.doubleValue())
        case Some(_) => println("result not a number")
        case None => println("failed to decode json")
      }
  }

  system.actorOf(Props(new UserObserverActor), "user-observer")
  val userMgmt = system.actorOf(Props(new UserMgmtActor), "user-group")

  val reader = new ConsoleReader()
  reader.setPrompt("system> ")

  while(true) {

    val line: String = reader.readLine()
    line match {
      case "create user" =>
        userMgmt ! UserMgmtActor.Messages.Commands.CreateUser
      case setPasswordMessage:String if setPasswordMessage.startsWith("set user password ") =>
        val r = setPasswordMessage.replaceAll("set user password ", "").split(" ").toList
        if (r.size == 2) {
          val userId = r.head
          val newPassword = r.last
          system.actorSelection("/user/user-group/user-" + userId).resolveOne().onComplete {
            case Success(someUserActor: ActorRef) =>
              someUserActor ! SetPassword(newPassword)
            case Failure(_) =>
              logger.info("failed to find user with id " + userId)
          }
        }
      case failUserMessage:String if failUserMessage.startsWith("fail ") =>
        val r = failUserMessage.split(" ").toList
        if (r.size == 2) {
          val userId = r.last
          system.actorSelection("/user/user-group/user-" + userId).resolveOne().onComplete {
            case Success(someUserActor: ActorRef) =>
              someUserActor ! UserActor.Messages.Commands.Fail
            case Failure(_) =>
              logger.info("failed to find user with id " + userId)
          }

        }

      case _ =>
        // do nothing
    }
  }
}



