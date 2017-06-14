package org.jamieallen.effectiveakka.pattern.extra

import akka.actor.{Actor, ActorRef}
import org.jamieallen.effectiveakka.common.Common._

class AccountBalanceRetriever2(savingsAccounts: ActorRef, checkingAccounts: ActorRef, moneyMarketAccounts: ActorRef) extends Actor {
  val checkingBalances, savingsBalances, mmBalances: Option[BalanceTable] = None
  var originalSender: Option[ActorRef] = None

  def receive = {
    case GetCustomerAccountBalances(id) =>
      originalSender = Some(sender)
      savingsAccounts ! GetCustomerAccountBalances(id)
      checkingAccounts ! GetCustomerAccountBalances(id)
      moneyMarketAccounts ! GetCustomerAccountBalances(id)
    case it@AccountBalances(Some(_), Some(_), Some(_)) => {
      originalSender.get ! it
    }
  }
}
