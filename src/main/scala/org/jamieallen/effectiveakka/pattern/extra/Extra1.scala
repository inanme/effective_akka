package org.jamieallen.effectiveakka.pattern.extra

import akka.actor.{Actor, ActorRef}
import akka.pattern.ask
import akka.util.Timeout
import org.jamieallen.effectiveakka.common.Common._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class AccountBalanceRetriever1(savingsAccounts: ActorRef, checkingAccounts: ActorRef, moneyMarketAccounts: ActorRef) extends Actor {
  implicit val timeout: Timeout = 100 milliseconds
  implicit val ec: ExecutionContext = context.dispatcher

  def receive = {
    case GetCustomerAccountBalances(id) =>
      val futSavings = savingsAccounts ? GetCustomerAccountBalances(id)
      val futChecking = checkingAccounts ? GetCustomerAccountBalances(id)
      val futMM = moneyMarketAccounts ? GetCustomerAccountBalances(id)
      val futBalances = for {
        savings <- futSavings.mapTo[Option[BalanceTable]]
        checking <- futChecking.mapTo[Option[BalanceTable]]
        mm <- futMM.mapTo[Option[BalanceTable]]
      } yield AccountBalances(savings, checking, mm)
      futBalances map (sender ! _)
  }
}
