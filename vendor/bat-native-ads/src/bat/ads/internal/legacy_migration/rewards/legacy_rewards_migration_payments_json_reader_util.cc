/* Copyright (c) 2021 The Brave Authors. All rights reserved.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

#include "bat/ads/internal/legacy_migration/rewards/legacy_rewards_migration_payments_json_reader_util.h"

#include <string>

#include "base/values.h"
#include "bat/ads/internal/legacy_migration/rewards/payment_info.h"
#include "third_party/abseil-cpp/absl/types/optional.h"

namespace ads {
namespace rewards {
namespace JSONReader {

namespace {

const char kPaymentListKey[] = "payments";
const char kBalanceKey[] = "balance";
const char kMonthKey[] = "month";
const char kTransactionCountKey[] = "transaction_count";

absl::optional<PaymentInfo> ParsePayment(const base::Value& value) {
  PaymentInfo payment;

  // Balance
  const absl::optional<double> balance_optional =
      value.FindDoubleKey(kBalanceKey);
  if (!balance_optional) {
    return absl::nullopt;
  }
  payment.balance = balance_optional.value();

  // Month
  const std::string* month_optional = value.FindStringKey(kMonthKey);
  if (!month_optional) {
    return absl::nullopt;
  }
  payment.month = *month_optional;

  // Transaction count
  const absl::optional<int> transaction_count_optional =
      value.FindIntKey(kTransactionCountKey);
  if (!transaction_count_optional) {
    return absl::nullopt;
  }
  payment.transaction_count = transaction_count_optional.value();

  return payment;
}

absl::optional<PaymentList> GetPaymentsFromList(const base::Value& value) {
  if (!value.is_list()) {
    return absl::nullopt;
  }

  PaymentList payments;

  for (const auto& payment_value : value.GetList()) {
    if (!payment_value.is_dict()) {
      return absl::nullopt;
    }

    const absl::optional<PaymentInfo>& payment_optional =
        ParsePayment(payment_value);
    if (!payment_optional) {
      return absl::nullopt;
    }
    const PaymentInfo& payment = payment_optional.value();

    payments.push_back(payment);
  }

  return payments;
}

}  // namespace

absl::optional<PaymentList> ParsePayments(const base::Value& value) {
  const base::Value* const payments_value = value.FindListKey(kPaymentListKey);
  if (!payments_value) {
    return absl::nullopt;
  }

  const absl::optional<PaymentList>& payments_optional =
      GetPaymentsFromList(*payments_value);
  if (!payments_optional) {
    return absl::nullopt;
  }
  const PaymentList& payments = payments_optional.value();

  return payments;
}

}  // namespace JSONReader
}  // namespace rewards
}  // namespace ads
