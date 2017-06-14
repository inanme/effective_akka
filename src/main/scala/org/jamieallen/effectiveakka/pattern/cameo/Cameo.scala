package org.jamieallen.effectiveakka.pattern.cameo

import akka.actor.{Actor, ActorLogging, ActorRef, Props, actorRef2Scala}
import akka.event.LoggingReceive
import org.jamieallen.effectiveakka.common.Common._

import scala.concurrent.duration.DurationInt

object AccountBalanceResponseHandler {

  case object AccountRetrievalTimeout

  def props(savingsAccounts: ActorRef,
            checkingAccounts: ActorRef,
            moneyMarketAccounts: ActorRef,
            originalSender: ActorRef): Props = {
    Props(new AccountBalanceResponseHandler(savingsAccounts, checkingAccounts,
      moneyMarketAccounts, originalSender))
  }
}

class AccountBalanceResponseHandler(savingsAccounts: ActorRef,
                                    checkingAccounts: ActorRef,
                                    moneyMarketAccounts: ActorRef,
                                    originalSender: ActorRef) extends Actor with ActorLogging {

  import AccountBalanceResponseHandler._

  var checkingBalances, savingsBalances, mmBalances: Option[BalanceTable] = None

  def receive = LoggingReceive {
    case CheckingAccountBalances(balances) =>
      log.debug(s"Received checking account balances: $balances")
      checkingBalances = balances
      collectBalances
    case SavingsAccountBalances(balances) =>
      log.debug(s"Received savings account balances: $balances")
      savingsBalances = balances
      collectBalances
    case MoneyMarketAccountBalances(balances) =>
      log.debug(s"Received money market account balances: $balances")
      mmBalances = balances
      collectBalances
    case AccountRetrievalTimeout =>
      log.debug("Timeout occurred")
      sendResponseAndShutdown(AccountRetrievalTimeout)
  }

  def collectBalances = (checkingBalances, savingsBalances, mmBalances) match {
    case (Some(_), Some(_), Some(_)) =>
      log.debug(s"Values received for all three account types")
      timeoutMessager.cancel
      sendResponseAndShutdown(AccountBalances(checkingBalances, savingsBalances, mmBalances))
    case _ =>
  }

  def sendResponseAndShutdown(response: Any) = {
    originalSender ! response
    log.debug("Stopping context capturing actor")
    context.stop(self)
  }

  import context.dispatcher

  val timeoutMessager = context.system.scheduler.scheduleOnce(250 milliseconds, self, AccountRetrievalTimeout)
}

class AccountBalanceRetriever(savingsAccounts: ActorRef, checkingAccounts: ActorRef, moneyMarketAccounts: ActorRef) extends Actor {
  def receive = {
    case GetCustomerAccountBalances(id) =>
      val originalSender = sender
      val handler = context.actorOf(AccountBalanceResponseHandler.props(savingsAccounts, checkingAccounts, moneyMarketAccounts, originalSender), "cameo-message-handler")
      savingsAccounts.tell(GetCustomerAccountBalances(id), handler)
      checkingAccounts.tell(GetCustomerAccountBalances(id), handler)
      moneyMarketAccounts.tell(GetCustomerAccountBalances(id), handler)
  }
}
