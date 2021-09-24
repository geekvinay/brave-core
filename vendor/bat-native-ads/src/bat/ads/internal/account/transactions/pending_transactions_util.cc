/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/account/transactions/pending_transactions_util.h"

#include <algorithm>
#include <iterator>

#include "base/time/time.h"
#include "bat/ads/internal/time_util.h"
#include "bat/ads/transaction_info.h"

namespace ads {

bool HasPendingTransactionsForDateRange(const TransactionList& transactions,
                                        const base::Time& from_time,
                                        const base::Time& to_time) {
  const int count =
      std::count_if(transactions.cbegin(), transactions.cend(),
                    [&from_time, &to_time](const TransactionInfo& transaction) {
                      return IsTransactionPendingForDateRange(
                          transaction, from_time, to_time);
                    });

  if (count == 0) {
    return false;
  }

  return true;
}

bool HasPendingTransactionsForPreviousMonth(
    const base::Time& time,
    const TransactionList& transactions) {
  const base::Time& from_time = AdjustTimeToBeginningOfPreviousMonth(time);
  const base::Time& to_time = AdjustTimeToEndOfPreviousMonth(time);
  return HasPendingTransactionsForDateRange(transactions, from_time, to_time);
}

bool HasPendingTransactionsForThisMonth(const base::Time& time,
                                        const TransactionList& transactions) {
  const base::Time& from_time = AdjustTimeToBeginningOfMonth(time);
  const base::Time& to_time = base::Time::Now();
  return HasPendingTransactionsForDateRange(transactions, from_time, to_time);
}

bool IsTransactionPending(const TransactionInfo& transaction) {
  const base::Time& redeemed_at =
      base::Time::FromDoubleT(transaction.redeemed_at);

  return redeemed_at.is_null();
}

bool IsTransactionPendingForDateRange(const TransactionInfo& transaction,
                                      const base::Time& from_time,
                                      const base::Time& to_time) {
  const base::Time& created_at =
      base::Time::FromDoubleT(transaction.created_at);

  return IsTransactionPending(transaction) && created_at >= from_time &&
         created_at <= to_time;
}

}  // namespace ads
