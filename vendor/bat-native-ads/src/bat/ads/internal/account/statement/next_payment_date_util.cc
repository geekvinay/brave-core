/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/account/statement/next_payment_date_util.h"

#include <algorithm>

#include "base/time/time.h"
#include "bat/ads/internal/account/transactions/pending_transactions_util.h"
#include "bat/ads/internal/features/ad_rewards/ad_rewards_features.h"

namespace ads {

base::Time CalculateNextPaymentDate(const base::Time& next_token_redemption_at,
                                    const TransactionList& transactions) {
  const base::Time now = base::Time::Now();

  base::Time::Exploded now_exploded;
  now.UTCExplode(&now_exploded);
  DCHECK(now_exploded.HasValidValues());

  int month = now_exploded.month;

  if (now_exploded.day_of_month <= features::GetAdRewardsNextPaymentDay()) {
    if (HasPendingTransactionsForPreviousMonth(now, transactions)) {
      // If last month has a pending balance, then the next payment date will
      // occur this month
    } else {
      // If last month does not have a pending balance, then the next payment
      // date will occur next month
      month++;
    }
  } else {
    if (HasPendingTransactionsForThisMonth(now, transactions)) {
      // If this month has a pending balance, then the next payment date will
      // occur next month
      month++;
    } else {
      base::Time::Exploded next_token_redemption_at_exploded;
      next_token_redemption_at.UTCExplode(&next_token_redemption_at_exploded);
      DCHECK(next_token_redemption_at_exploded.HasValidValues());

      if (next_token_redemption_at_exploded.month == month) {
        // If this month does not have a pending balance and our next token
        // redemption date is this month, then the next payment date will occur
        // next month
        month++;
      } else {
        // If this month does not have a pending balance and our next token
        // redemption date is next month, then the next payment date will occur
        // the month after next
        month += 2;
      }
    }
  }

  int year = now_exploded.year;

  if (month > 12) {
    month -= 12;
    year++;
  }

  base::Time::Exploded next_payment_date_exploded = now_exploded;
  next_payment_date_exploded.year = year;
  next_payment_date_exploded.month = month;
  next_payment_date_exploded.day_of_month =
      features::GetAdRewardsNextPaymentDay();

  base::Time next_payment_date;
  const bool success = base::Time::FromUTCExploded(next_payment_date_exploded,
                                                   &next_payment_date);
  DCHECK(success);

  return next_payment_date;
}

}  // namespace ads
