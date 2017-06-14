package org.jamieallen.effectiveakka.common

import akka.actor.Actor

object Common {
  type AccountNumber = Long
  type CustomerNumber = Long
  type Balance = BigDecimal
  type BalanceTable = List[(AccountNumber, Balance)]
  type AccountTable = Map[AccountNumber, BalanceTable]

  case class GetCustomerAccountBalances(id: CustomerNumber)

  case class AccountBalances(checking: Option[BalanceTable],
                             savings: Option[BalanceTable],
                             moneyMarket: Option[BalanceTable])

  case class CheckingAccountBalances(balances: Option[BalanceTable])

  case class SavingsAccountBalances(balances: Option[BalanceTable])

  case class MoneyMarketAccountBalances(balances: Option[BalanceTable])

  trait SavingsAccountsProxy extends Actor

  trait CheckingAccountsProxy extends Actor

  trait MoneyMarketAccountsProxy extends Actor

}
