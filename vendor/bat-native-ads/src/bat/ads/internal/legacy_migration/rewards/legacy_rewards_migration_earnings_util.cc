/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_earnings_util.h"

#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_payments_util.h"
#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_transaction_util.h"

namespace ads {
namespace rewards {

double GetEarningsForTransactions(const TransactionList& transactions) {
  double earnings = 0.0;

  for (const auto& transaction : transactions) {
    earnings += transaction.value;
  }

  return earnings;
}

double GetEarningsForPreviousMonths(const TransactionList& transactions) {
  const TransactionList& transactions_for_previous_months =
      GetTransactionsForPreviousMonths(transactions);

  return GetEarningsForTransactions(transactions_for_previous_months);
}

double GetUnredeemedEarningsForPreviousMonths(
    const TransactionList& transactions,
    const PaymentList& payments) {
  const double earnings_for_previous_months =
      GetEarningsForPreviousMonths(transactions);

  const double payment_balance_for_previous_months =
      GetPaymentBalanceForPreviousMonths(payments);

  return earnings_for_previous_months - payment_balance_for_previous_months;
}

}  // namespace rewards
}  // namespace ads
