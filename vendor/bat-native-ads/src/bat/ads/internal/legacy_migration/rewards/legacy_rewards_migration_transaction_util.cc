/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_transaction_util.h"

#include "base/guid.h"
#include "base/time/time.h"
#include "bat/ads/confirmation_type.h"
#include "bat/ads/internal/account/transactions/transactions_util.h"
#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_earnings_util.h"
#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_payments_util.h"
#include "bat/ads/internal/time_util.h"
#include "bat/ads/transaction_info.h"

namespace ads {
namespace rewards {

TransactionList GetTransactionsForPreviousMonths(
    const TransactionList& transactions) {
  const base::Time& from_time = base::Time();
  const base::Time& to_time = GetTimeAtEndOfLastMonth();

  return GetTransactionsForDateRange(transactions, from_time, to_time);
}

TransactionList GetTransactionsForThisMonth(
    const TransactionList& transactions) {
  const base::Time& from_time = GetTimeAtBeginningOfThisMonth();
  const base::Time& to_time = base::Time::Now();

  return GetTransactionsForDateRange(transactions, from_time, to_time);
}

TransactionInfo BuildUnredeemedTransactionForPreviousMonths(
    const TransactionList& transactions,
    const PaymentList& payments) {
  const base::Time& created_at = GetTimeAtBeginningOfLastMonth();

  TransactionInfo transaction;
  transaction.id = base::GenerateGUID();
  transaction.created_at = created_at.ToDoubleT();
  transaction.value =
      GetUnredeemedEarningsForPreviousMonths(transactions, payments);
  transaction.confirmation_type = ConfirmationType::kViewed;
  transaction.redeemed_at = base::Time().ToDoubleT();

  return transaction;
}

TransactionInfo BuildRedeemedTransactionForLastMonth(
    const PaymentList& payments) {
  const base::Time& adjusted_time = GetTimeAtBeginningOfLastMonth();
  const double adjusted_timestamp = adjusted_time.ToDoubleT();

  TransactionInfo transaction;
  transaction.id = base::GenerateGUID();
  transaction.created_at = adjusted_timestamp;
  transaction.value = GetPaymentBalanceForMonth(payments, adjusted_time);
  transaction.confirmation_type = ConfirmationType::kViewed;
  transaction.redeemed_at = adjusted_timestamp;

  return transaction;
}

}  // namespace rewards
}  // namespace ads
