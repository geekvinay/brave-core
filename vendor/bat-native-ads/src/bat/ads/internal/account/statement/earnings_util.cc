/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/account/statement/earnings_util.h"

#include "base/time/time.h"
#include "bat/ads/internal/account/transactions/pending_transactions_util.h"
#include "bat/ads/transaction_info.h"

namespace ads {

double GetEarningsForDateRange(const TransactionList& transactions,
                               const base::Time& from_time,
                               const base::Time& to_time) {
  const double from_timestamp = from_time.ToDoubleT();
  const double to_timestamp = to_time.ToDoubleT();

  double earnings = 0.0;

  for (const auto& transaction : transactions) {
    if (transaction.created_at < from_timestamp ||
        transaction.created_at > to_timestamp) {
      continue;
    }

    earnings += transaction.value;
  }

  return earnings;
}

double GetUnredeemedEarningsForDateRange(const TransactionList& transactions,
                                         const base::Time& from_time,
                                         const base::Time& to_time) {
  double earnings = 0.0;

  for (const auto& transaction : transactions) {
    if (!IsTransactionPendingForDateRange(transaction, from_time, to_time)) {
      continue;
    }

    earnings += transaction.value;
  }

  return earnings;
}

}  // namespace ads
