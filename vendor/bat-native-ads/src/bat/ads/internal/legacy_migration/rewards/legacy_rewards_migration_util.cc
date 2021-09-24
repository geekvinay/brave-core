/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_util.h"

#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_payments_json_reader.h"
#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_payments_util.h"
#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_transaction_history_json_reader.h"
#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_transaction_util.h"
#include "bat/ads/internal/legacy_migration/rewards/payment_info_aliases.h"
#include "bat/ads/transaction_info.h"
#include "third_party/abseil-cpp/absl/types/optional.h"

namespace ads {
namespace rewards {

absl::optional<TransactionList> BuildTransactionsFromJson(
    const std::string& json) {
  const absl::optional<PaymentList>& payments_optional =
      JSONReader::ReadPayments(json);
  if (!payments_optional) {
    return absl::nullopt;
  }
  const PaymentList& payments = payments_optional.value();

  const absl::optional<TransactionList>& transaction_history_optional =
      JSONReader::ReadTransactionHistory(json);
  if (!transaction_history_optional) {
    return absl::nullopt;
  }
  const TransactionList& transaction_history =
      transaction_history_optional.value();

  // Get a list of transactions for this month
  TransactionList transactions =
      GetTransactionsForThisMonth(transaction_history);

  // Append a single transaction with an accumulated value for unredeemed
  // unblinded payment tokens for previous months as we need to know if we had
  // open transactions for last month when calculating the next payment date and
  // if the user has an outstanding balance which should be carried over from
  // previous months
  const TransactionInfo& unredeemed_transaction =
      BuildUnredeemedTransactionForPreviousMonths(transaction_history,
                                                  payments);
  transactions.push_back(unredeemed_transaction);

  // Append a single transaction with an accumulated value for redeemed
  // unblinded payment tokens for last month as we need to know if we had
  // pending transactions for last month when calculating the next payment date
  const TransactionInfo& redeemed_transaction =
      BuildRedeemedTransactionForLastMonth(payments);
  transactions.push_back(redeemed_transaction);

  return transactions;
}

}  // namespace rewards
}  // namespace ads
