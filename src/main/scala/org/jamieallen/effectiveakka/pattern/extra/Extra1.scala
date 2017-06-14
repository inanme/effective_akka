package org.jamieallen.effectiveakka.pattern.extra

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import org.jamieallen.effectiveakka.common.Common._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.control.NonFatal

class AccountBalanceRetriever1(savingsAccounts: ActorRef, checkingAccounts: ActorRef, moneyMarketAccounts: ActorRef) extends Actor with ActorLogging {
  implicit val timeout: Timeout = 100 milliseconds
  implicit val ec: ExecutionContext = context.dispatcher

  def receive = LoggingReceive {
    case GetCustomerAccountBalances(id) =>
      log.debug(s"Received GetCustomerAccountBalances for ID: $id from $sender")
      val originalSender = sender

      val futSavings = savingsAccounts ? GetCustomerAccountBalances(id)
      val futChecking = checkingAccounts ? GetCustomerAccountBalances(id)
      val futMarket = moneyMarketAccounts ? GetCustomerAccountBalances(id)
      val futBalances = for {
        savings <- futSavings.mapTo[SavingsAccountBalances]
        checking <- futChecking.mapTo[CheckingAccountBalances]
        mm <- futMarket.mapTo[MoneyMarketAccountBalances]
      } yield AccountBalances(checking.balances, savings.balances, mm.balances)
      futBalances map (result => {
        log.info("mert1" + result)
        originalSender ! result
        log.debug("Stopping context capturing actor")
        context.stop(self)
      }) recover {
        case NonFatal(ex) => {
          log.error(ex, "mert2")
          originalSender ! AccountBalances(None, None, None)
        }
      }
  }

}
